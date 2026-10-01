# ADR 0004 — Synchronisation idempotente par boîte d'envoi

- Statut : accepté

## Contexte

Au retour de la connexion, les ventes doivent rejoindre Firebase **sans perte et sans doublon**,
alors que le réseau peut tomber au milieu d'un envoi et que l'application peut être tuée à tout moment.

## Décision

1. **Boîte d'envoi.** Une vente naît `sync_status = PENDING`. `SalesSynchronizer` envoie les ventes
   `PENDING` par lots de 50, dans l'ordre des numéros.
2. **Clé déterministe.** Chaque vente a un UUID créé sur la tablette, utilisé comme clé Firebase
   (`/stores/{storeId}/sales/{uuid}`). Renvoyer la même vente réécrit le même nœud : **pas de doublon**,
   quel que soit le nombre de tentatives.
3. **Écriture atomique.** Une seule mise à jour multi-chemins écrit la vente et `ticketIndex/{numéro}`.
   Soit les deux passent, soit aucun.
4. **Acquittement d'abord.** La vente ne passe à `SYNCED` qu'après l'accusé de réception du serveur :
   **pas de perte**. Si l'application meurt entre l'accusé et la mise à jour locale, la vente est
   renvoyée, ce qui est sans effet grâce au point 2.
5. **Erreurs.**
   - Réseau, délai dépassé (30 s, Firebase attend au lieu d'échouer hors ligne) : arrêt du lot, `Result.retry()`
     avec backoff exponentiel.
   - Refus des règles (`PERMISSION_DENIED`) : ce n'est pas forcément un conflit (session expirée, règles mal
     déployées…). La synchronisation lit `ticketIndex/{numéro}` : s'il pointe vers cette vente, elle est déjà
     sur le serveur (`SYNCED`). S'il pointe vers une autre vente, c'est un vrai conflit (`CONFLICT`, visible
     et relançable depuis l'historique). Sinon, on réessaie plus tard.

## Déclenchement

- Après chaque encaissement, au démarrage, et toutes les 15 minutes en filet de sécurité.
- Contrainte réseau WorkManager : le travail part dès que la connexion revient, même si l'application est
  fermée.
- Politique `APPEND_OR_REPLACE` : si un worker tourne déjà, un nouveau est chaîné derrière lui.
  `KEEP` perdrait la demande si ce worker avait déjà lu une boîte vide. Si un worker est déjà en
  attente, rien n'est ajouté : il verra la nouvelle vente, déjà validée en base. La chaîne ne
  dépasse donc jamais deux éléments.
- Un `Mutex` évite que le worker périodique et le worker ponctuel envoient en même temps.

## Règles de sécurité

Voir `firebase/database.rules.json` et ses tests (`firebase/tests`). En résumé : lecture et écriture
réservées aux utilisateurs authentifiés. Une vente ne peut être écrite que par la caisse qui possède le
registre. Une vente synchronisée ne peut plus être modifiée ni supprimée. Un numéro de ticket ne peut
désigner qu'une seule vente. Les droits d'écriture ne sont donnés qu'aux feuilles de l'arbre, car les
règles RTDB s'héritent et ne se retirent pas.
