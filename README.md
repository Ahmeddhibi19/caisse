# Caisse

Mini application de caisse Android en Kotlin, **utilisable avec ou sans connexion**.
Huit produits, un panier, un bouton **Encaisser**, un historique des ventes, et une synchronisation
Firebase Realtime Database sans perte ni doublon.

[![CI](https://github.com/Ahmeddhibi19/caisse/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/Ahmeddhibi19/caisse/actions/workflows/ci.yml)

- [Fonctionnalités](#fonctionnalités)
- [Lancer l'application](#lancer-lapplication)
- [Architecture](#architecture)
- [Numérotation des tickets](#numérotation-des-tickets)
- [Impression](#impression)
- [Hors ligne et synchronisation](#hors-ligne-et-synchronisation)
- [Tests et intégration continue](#tests-et-intégration-continue)
- [Organisation du dépôt](#organisation-du-dépôt)
- [Scénarios de démonstration](#scénarios-de-démonstration)
- [Limites et pistes](#limites-et-pistes)

## Fonctionnalités

- **Caisse** : grille de 8 produits, panier modifiable, total, bouton « Encaisser ». Sur tablette,
  produits et panier côte à côte ; sur téléphone, l'un au-dessus de l'autre.
- **Encaisser, en un seul geste** :
  1. attribue un numéro de ticket unique et séquentiel (`C02-000137`) ;
  2. enregistre la vente durablement (transaction SQLite) ;
  3. envoie le ticket à l'impression ;
  4. vide le panier et rend la main immédiatement.
- **Historique** : numéro, date, montant, état d'impression (*En attente* / *Imprimé* / *Échec*) et état
  de synchronisation. On peut réimprimer un ticket en échec et voir l'aperçu du ticket.
- **Reprise au démarrage** : les tickets en attente ou en échec repartent à l'impression, jamais ceux
  déjà imprimés.
- **Hors ligne** : encaissement, numérotation, impression et historique fonctionnent sans réseau. Au retour
  de la connexion, les ventes partent vers Firebase sans perte et sans doublon. Un indicateur affiche
  l'état du réseau et le nombre de ventes à synchroniser.

## Lancer l'application

### Installer l'APK

Chaque build de la CI publie un APK de debug (onglet *Actions* → run → *Artifacts* → `caisse-debug-apk`).
Chaque version taguée publie aussi un APK dans les
[Releases](https://github.com/Ahmeddhibi19/caisse/releases).

Au **premier lancement**, la tablette doit être connectée : elle réserve son numéro de caisse auprès du
serveur (une seule fois, voir plus bas). Ensuite, tout fonctionne hors ligne.

### Compiler

Prérequis : JDK 21 et le SDK Android (API 37), ou simplement Android Studio.

```bash
./gradlew assembleDebug          # APK : app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # tests unitaires (JVM + Robolectric)
./gradlew lintDebug
```

### Projet Firebase

Le fichier `app/google-services.json` est versionné : la configuration d'un client Firebase n'est pas
un secret, l'accès aux données est protégé par les règles de sécurité et l'authentification. Pour utiliser
votre propre projet :

1. Créer un projet Firebase, puis une **Realtime Database**.
2. Activer **Authentication → Anonymous**.
3. Ajouter une application Android `com.ahmeddhibi.caisse` et remplacer `app/google-services.json`.
4. Renseigner l'URL de la base dans `gradle.properties` (`caisse.firebaseDatabaseUrl`).
5. Déployer les règles :

   ```bash
   cd firebase
   npm ci
   npx firebase login
   npx firebase use --add      # choisir le projet
   npm run deploy:rules
   ```

### Émulateurs Firebase (optionnel)

```bash
cd firebase
npx firebase emulators:start --only auth,database --project demo-caisse
./gradlew installDebug -Pcaisse.useFirebaseEmulator=true   # depuis la racine
```

L'application de debug se connecte alors à `10.0.2.2` (l'hôte vu depuis l'émulateur Android).

## Architecture

Un module `:app`, organisé en couches `ui` → `domain` ← `data`, avec MVVM côté interface.

| Couche | Contenu |
|---|---|
| `ui` | Jetpack Compose (Material 3), ViewModels exposant un `StateFlow<UiState>`, Navigation Compose typée |
| `domain` | Modèles (`Money` en centimes, `Cart`, `Sale`, `TicketNumber`), interfaces, cas d'usage (`CheckoutUseCase`…) |
| `data` | Room (source de vérité), Firebase (Auth anonyme + Realtime Database), file d'impression, synchronisation WorkManager |
| `core` | Injection Hilt, portée applicative, configuration, état du réseau, formatage |

Stack : Kotlin 2.3, AGP 9 (Kotlin intégré), Compose, Hilt, Room 2.8 (KSP), Coroutines / Flow,
WorkManager, DataStore, Firebase BoM 34. `minSdk` 26, `targetSdk` 36.

Diagrammes (couches, séquence d'encaissement, modèle de données) : [docs/architecture.md](docs/architecture.md).

## Numérotation des tickets

C'est le point délicat : le numéro est imprimé tout de suite, il doit être unique même si plusieurs
tablettes vendent hors ligne en même temps, et il ne pourra plus changer.

**Choix : un numéro par caisse.** `C02-000137` = 137ᵉ vente de la caisse n° 2.

- Le **numéro de caisse** est attribué **une seule fois**, au premier lancement, par une transaction
  Firebase sur un compteur, puis réservé (écriture unique) au nom de la tablette.
- La **séquence** est locale. Une même transaction SQLite l'incrémente et enregistre la vente : pas de
  trou, pas de doublon, même si l'application est tuée au mauvais moment. Les index uniques et
  `PRAGMA synchronous = FULL` (pas de vente perdue sur coupure de courant) complètent la garantie.
- Le serveur vérifie aussi : un numéro de ticket ne peut désigner qu'une seule vente.

**Pourquoi.** Deux tablettes n'ont jamais le même préfixe : l'unicité ne dépend pas du réseau.
La séquence par terminal est aussi le modèle des caisses certifiées (NF525).

**Limites.**
- Pas de numérotation unique pour tout le magasin : chaque caisse a la sienne.
- Le tout premier lancement exige une connexion (l'écran attend et reprend tout seul).
- Effacer les données de l'application crée une nouvelle caisse. Les ventes non synchronisées de
  l'ancienne installation sont perdues.
- Deux tablettes hors ligne en même temps ne peuvent pas entrer en collision. Seule une tablette clonée
  le pourrait : la sauvegarde et le transfert de données sont désactivés, et le serveur refuse les
  doublons (visibles en *Conflit*).

Alternatives étudiées et écartées (compteur global, renumérotation, plages réservées, UUID) :
[ADR 0002](docs/adr/0002-numerotation-des-tickets.md).

## Impression

- La table des ventes sert de **file d'impression** : rien n'est perdu si l'application s'arrête.
- `PrintSpooler` imprime les tickets un par un, dans l'ordre, avec un délai maximum de 15 s.
- Au démarrage, `FAILED` et `PRINTING` (impression interrompue) repassent `PENDING`. `PRINTED` n'est
  jamais réimprimé.
- Garantie **au moins une fois** : un ticket ne peut pas être perdu. Il ne peut sortir en double que si
  l'application meurt exactement entre la sortie du papier et l'écriture en base.
- L'imprimante est **simulée** derrière l'interface `TicketPrinter`. L'icône réglages de la caisse permet
  de choisir *Normale*, *Instable* (environ un ticket sur trois échoue) ou *Hors ligne*. Le ticket
  (32 colonnes) apparaît dans Logcat, tag `TicketPrinter`.

Détails : [ADR 0003](docs/adr/0003-impression-au-moins-une-fois.md).

## Hors ligne et synchronisation

- **Room est la source de vérité.** Firebase reçoit une copie (persistance hors ligne Firebase désactivée
  volontairement, voir [ADR 0001](docs/adr/0001-room-source-de-verite.md)).
- Chaque vente naît « à synchroniser ». Un `Worker` WorkManager, soumis à une contrainte réseau, les envoie
  dès que la connexion revient, même application fermée : après chaque vente, au démarrage et toutes les
  15 minutes.
- **Sans doublon** : la clé Firebase est l'UUID créé sur la tablette, donc un renvoi réécrit le même nœud.
- **Sans perte** : une vente ne passe « synchronisée » qu'après l'accusé de réception du serveur.
- Un refus du serveur est analysé avant d'être classé en conflit : une session expirée n'est pas un doublon.

Détails : [ADR 0004](docs/adr/0004-synchronisation-idempotente.md).

## Tests et intégration continue

- **Tests unitaires** (JUnit, Truth, Turbine, coroutines-test), dont les DAO Room et la file d'impression
  avec **Robolectric** sur une base en mémoire :
  - 100 encaissements concurrents donnent les numéros 1 à 100, sans trou ;
  - une vente qui échoue ne consomme pas de numéro ;
  - le redémarrage ne réimprime jamais un ticket imprimé ;
  - un double appui sur « Encaisser » ne crée qu'une vente ;
  - les erreurs réseau et les refus du serveur sont distingués à la synchronisation ;
  - le schéma Room exporté est à jour.
- **Règles Firebase** testées contre l'émulateur (`firebase/tests`, `@firebase/rules-unit-testing`).
- **GitHub Actions** :
  - `ci.yml` (chaque push et PR) : build, lint, tests, règles, APK en artefact ;
  - `release.yml` (tag `v*`) : release GitHub avec l'APK.

## Organisation du dépôt

Branches *git-flow* : `main` (versions publiées), `develop` (intégration), `feature/*`, `release/*`,
`hotfix/*`. Chaque fonctionnalité a été développée sur sa branche et fusionnée dans `develop` sans
fast-forward.

```
app/                  application Android
  schemas/            schémas Room exportés
  src/main/java/com/ahmeddhibi/caisse/
    core/             DI, configuration, réseau, formatage
    domain/           modèles, interfaces, cas d'usage, ticket
    data/             Room, Firebase, impression, synchronisation
    ui/               écrans Compose et ViewModels
  src/test/           tests JVM et Robolectric
firebase/             règles de sécurité, configuration des émulateurs, tests des règles
docs/                 architecture et décisions (ADR)
.github/              CI, release, Dependabot
```

## Scénarios de démonstration

1. **Vente en ligne** : encaisser → `C01-000001`, *Imprimé*, puis *Synchronisé* (visible dans la console
   Firebase).
2. **Hors ligne** : mode avion, trois ventes → numéros qui se suivent, tickets imprimés, historique
   « À synchroniser ». Réseau rétabli → chaque vente apparaît une seule fois dans Firebase.
3. **Imprimante en panne** : réglages → *Hors ligne*, encaisser → *Échec*. Fermer l'application, repasser
   en *Normale*, relancer → le ticket est imprimé, les tickets déjà imprimés ne bougent pas.
4. **Deux tablettes** : deux appareils enregistrés (`C01`, `C02`), tous deux hors ligne, des ventes de
   chaque côté, puis reconnexion → toutes les ventes arrivent, aucune collision.
5. **Double appui** sur « Encaisser » → une seule vente.

## Limites et pistes

- Authentification anonyme : suffisante pour la démonstration. En production, il faudrait un compte
  magasin pour enregistrer une caisse et lire les ventes.
- L'imprimante est simulée. Un pilote ESC/POS (Bluetooth, USB ou réseau) s'ajoute derrière `TicketPrinter`.
- L'historique charge toutes les ventes. Au-delà de quelques milliers, il faudrait Paging 3.
- Pas encore de chaînage cryptographique des tickets (exigé par la certification NF525).
- Pas de tests instrumentés sur émulateur dans la CI (seulement JVM et Robolectric).
