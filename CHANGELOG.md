# Changelog

Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/), versions selon
[Semantic Versioning](https://semver.org/lang/fr/).

## [1.0.0] — 2026-10-01

### Ajouté

- Caisse : catalogue de 8 produits, panier modifiable, total, mise en page tablette et téléphone.
- « Encaisser » en un geste : numéro de ticket attribué et vente enregistrée dans la même transaction,
  impression lancée, panier vidé, main rendue aussitôt. Protection contre le double appui.
- Numérotation par caisse (`C02-000137`) : numéro de caisse réservé une seule fois auprès de Firebase,
  séquence locale sans trou, unicité vérifiée aussi par le serveur.
- Écran d'enregistrement de la caisse, qui attend le réseau au premier lancement et reprend seul.
- File d'impression persistante (au moins une fois), reprise au démarrage des tickets en attente ou en
  échec, réimpression manuelle, imprimante simulée avec modes Normale / Instable / Hors ligne.
- Historique des ventes : numéro, date, montant, état d'impression et de synchronisation, aperçu du ticket.
- Synchronisation Firebase Realtime Database par boîte d'envoi : sans perte, sans doublon, relance
  automatique au retour du réseau, conflits visibles et relançables.
- Indicateur réseau et nombre de ventes à synchroniser.
- Règles de sécurité Realtime Database et leurs tests sur l'émulateur.
- CI GitHub Actions (build, lint, tests JVM/Robolectric, règles, vérification du schéma Room) et
  publication d'un APK à chaque tag.

[1.0.0]: https://github.com/Ahmeddhibi19/caisse/releases/tag/v1.0.0
