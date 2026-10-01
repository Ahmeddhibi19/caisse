# ADR 0002 — Numérotation des tickets par caisse

- Statut : accepté
- Contexte : Caisse 1.0

## Contexte

Chaque vente doit recevoir **au moment de l'encaissement** un numéro de ticket unique et séquentiel,
imprimé sur le ticket papier. L'encaissement doit fonctionner sans réseau, y compris quand plusieurs
tablettes vendent hors ligne en même temps dans le même magasin.

Un numéro imprimé ne peut plus changer : toute solution qui « corrige » le numéro plus tard est exclue.

## Décision

Le numéro est composé de **l'identifiant de la caisse** et d'une **séquence locale** :

```
C02-000137
│   └──── 137ᵉ vente de cette caisse, sans trou
└──────── caisse n° 2, attribuée une seule fois par le serveur
```

1. **Enregistrement de la caisse (une seule fois, en ligne).** Au premier lancement, l'application
   s'authentifie anonymement puis incrémente `/stores/{storeId}/registerCounter` par une transaction
   Realtime Database. Le numéro obtenu est réservé en écrivant `/stores/{storeId}/registers/C02`
   (écriture unique, protégée par les règles). Il est ensuite stocké dans Room (`register_state`).
2. **Chaque vente (hors ligne possible).** Une seule transaction SQLite lit `last_sequence`,
   l'incrémente et insère la vente et ses lignes. Soit tout est écrit, soit rien : un numéro n'est
   ni perdu ni attribué deux fois, même si l'application est tuée au milieu.
3. **Garde-fous.**
   - Index uniques Room sur `(register_number, sequence)` et `ticket_number`.
   - `PRAGMA synchronous = FULL` : une coupure de courant ne peut pas annuler une vente dont le ticket
     est déjà imprimé (ce qui ferait réattribuer son numéro).
   - Côté serveur, `ticketIndex/{numéro}` est écrit dans la même mise à jour atomique que la vente et
     ne peut pointer que vers une seule vente : un doublon (caisse clonée…) est refusé et apparaît en
     « Conflit » dans l'historique.
   - Sauvegarde et transfert d'appareil désactivés (`allowBackup=false` et `data_extraction_rules`)
     pour qu'une caisse ne puisse pas être restaurée sur une autre tablette.

## Pourquoi ce choix

- **Unicité par construction** : deux tablettes ne partagent jamais de préfixe, elles peuvent vendre
  hors ligne aussi longtemps que nécessaire sans se coordonner.
- **Séquentiel et sans trou par caisse**, ce qu'attendent un comptable ou un contrôle fiscal : c'est le
  modèle des caisses certifiées (NF525 en France), où la numérotation est propre à chaque terminal.
- **Aucune dépendance réseau au moment de vendre**, seulement à l'installation, comme l'appairage
  d'un terminal de paiement.

## Alternatives écartées

| Approche | Pourquoi elle est écartée |
|---|---|
| Compteur global incrémenté par transaction Firebase à chaque vente | Impossible hors ligne : la transaction attend le serveur. |
| Numéro provisoire hors ligne, renuméroté à la synchronisation | Le ticket papier porte déjà le numéro provisoire. |
| Plages de numéros réservées à l'avance par chaque caisse | Trous quand une plage n'est pas consommée, épuisement si la caisse reste longtemps hors ligne, numéros non chronologiques entre caisses. |
| UUID | Unique mais ni lisible ni séquentiel. |
| Code de caisse aléatoire généré hors ligne | Unicité seulement probabiliste. Gardé comme piste si l'installation hors ligne devenait indispensable. |

## Limites

- **Pas de séquence unique pour tout le magasin** : `C01-000010` et `C02-000010` coexistent. L'ordre
  global se reconstitue avec la date de vente.
- **Le tout premier lancement nécessite le réseau.** L'écran d'enregistrement attend la connexion et
  reprend tout seul.
- **Effacer les données ou réinstaller l'application** crée une nouvelle caisse (nouveau numéro). Les
  ventes non synchronisées de l'ancienne installation sont perdues avec ses données.
- **Deux tablettes hors ligne en même temps** : aucun risque de collision (préfixes différents). Le seul
  cas de doublon est une caisse clonée (données copiées sur une autre tablette, par exemple avec un
  appareil rooté) : le serveur refuse alors les ventes en double, qui restent visibles en conflit.
- L'authentification anonyme suffit pour la démonstration. En production, l'enregistrement d'une caisse
  serait réservé à un compte gérant.
- La date de vente est celle de l'appareil. L'historique est trié par numéro, pas par date, et le
  serveur ajoute `syncedAt` avec son horloge.
