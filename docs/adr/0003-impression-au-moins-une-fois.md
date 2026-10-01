# ADR 0003 — File d'impression en base, impression « au moins une fois »

- Statut : accepté

## Contexte

« Encaisser » doit envoyer le ticket à l'imprimante et rendre la main immédiatement. Au démarrage, les
tickets en attente ou en échec doivent repartir à l'impression, jamais ceux déjà imprimés. L'imprimante
peut être lente, débranchée, à court de papier, et l'application peut être tuée à tout moment.

## Décision

La table `sales` **est** la file d'impression. Il n'y a pas de file en mémoire qui pourrait se perdre.

```
            claimNextPrint()            imprimante OK
 PENDING ───────────────────▶ PRINTING ───────────────▶ PRINTED
    ▲                            │
    │                            │ erreur / délai dépassé (15 s)
    │      « Réimprimer »        ▼
    └──────────────────────── FAILED

 Au démarrage (une fois par processus) : FAILED → PENDING, PRINTING → PENDING. PRINTED ne bouge jamais.
```

- `PrintSpooler` traite les tickets **un par un, dans l'ordre des numéros**. Il réserve le prochain ticket
  `PENDING` en le passant à `PRINTING` dans une transaction, l'envoie à l'imprimante, puis enregistre
  `PRINTED` ou `FAILED` avec la raison.
- Quand la file est vide, il attend sur un `Channel` *conflated* : un encaissement le réveille, sans
  scrutation et sans réveil perdu.
- `start()` est appelé par `MainActivity` et protégé par un `AtomicBoolean`. La remise en file des tickets
  interrompus se fait **dans** cette garde : recréer l'activité (rotation) ne remet pas en file le ticket
  en cours d'impression.
- Le démarrage n'est pas fait dans `Application.onCreate` : WorkManager peut lancer le processus en
  arrière-plan pour synchroniser, et l'imprimante ne doit pas se mettre à imprimer sans caissier.
- Un délai maximum (`withTimeoutOrNull`) empêche une imprimante muette de bloquer la file. Une erreur
  ne peut pas arrêter la boucle (chaque tour a son propre `try/catch`).

## Sémantique : au moins une fois

Un ticket ne passe à `PRINTED` qu'après la réponse de l'imprimante. Si l'application meurt **entre**
la sortie du papier et l'écriture en base, le ticket est encore `PRINTING` au redémarrage : il est
réimprimé. On peut donc avoir un duplicata dans ce cas très rare, mais **jamais de ticket perdu**.
Pour une caisse, c'est le bon compromis : un duplicata se jette, un ticket manquant pose problème.

## Imprimante

`TicketPrinter` est une interface (port). L'implémentation livrée est `SimulatedTicketPrinter`, avec trois
modes réglables depuis l'écran de caisse (Normale, Instable, Hors ligne) pour démontrer les échecs et la
reprise. Le ticket formaté (32 colonnes, imprimante 58 mm) est écrit dans Logcat (`TicketPrinter`).
Brancher une vraie imprimante ESC/POS (USB, Bluetooth ou réseau) revient à fournir une autre
implémentation dans `PrintingModule`.
