# ADR 0001 — Room comme source de vérité, Firebase comme destination

- Statut : accepté

## Contexte

La caisse doit fonctionner sans réseau : encaisser, numéroter, imprimer et consulter l'historique.
Firebase Realtime Database propose un cache disque et une file d'écritures hors ligne. Pourquoi ne pas
s'en contenter ?

## Décision

- **Room (SQLite) est la seule source de vérité** sur la tablette. L'interface lit Room, jamais Firebase.
- **Firebase reçoit une copie** des ventes au travers d'une boîte d'envoi (*outbox*) : la colonne
  `sync_status` des ventes, vidée par un `Worker` WorkManager.
- La persistance disque de Firebase reste **désactivée**.

## Pourquoi

| Besoin | Room | Cache hors ligne Firebase |
|---|---|---|
| Attribuer un numéro et enregistrer la vente atomiquement | Transaction SQLite | Les transactions RTDB exigent le serveur |
| Savoir quelles ventes restent à envoyer, en relancer une | Requête sur `sync_status` | File interne opaque |
| État d'impression local, requêtes de l'historique | Tables et index | Non prévu pour ça |
| Tests | Base en mémoire avec Robolectric | Difficile à isoler |

Garder les deux files (Room et la file de Firebase) aurait créé deux vérités à réconcilier. La file
d'écriture de Firebase n'est utilisée que le temps d'une synchronisation, et sa perte (processus tué) est
sans conséquence : la vente est encore `PENDING` dans Room et sera renvoyée.

## Conséquences

- Il faut écrire la synchronisation (voir ADR 0004), mais elle est explicite et testée.
- Le schéma Room est exporté (`app/schemas`) et vérifié par la CI pour ne jamais oublier une migration.
- `fallbackToDestructiveMigration` est volontairement absent : une migration manquante doit faire planter
  la build de test, pas effacer des ventes.
