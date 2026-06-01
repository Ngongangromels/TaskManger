# STRMS — Smart Task & Resource Management System

Projet pédagogique de programmation orientée objet en Java (E3e — S6 — Spring 2026).
Application complète de gestion de tâches avec interface graphique JavaFX.

> Charte graphique : **bleu / blanc**, inspiration Jira.

---

## Sommaire

- [1. Objectifs pédagogiques](#1-objectifs-pédagogiques)
- [2. Fonctionnalités](#2-fonctionnalités)
- [3. Architecture du projet](#3-architecture-du-projet)
- [4. Prérequis](#4-prérequis)
- [5. Comment exécuter le projet](#5-comment-exécuter-le-projet)
- [6. Comment exécuter les tests](#6-comment-exécuter-les-tests)
- [7. Utilisation de l'application](#7-utilisation-de-lapplication)
- [8. Diagrammes UML](#8-diagrammes-uml)
- [9. Choix techniques](#9-choix-techniques)

---

## 1. Objectifs pédagogiques

Ce projet illustre :

| Concept                       | Où le voir dans le code                                       |
|-------------------------------|---------------------------------------------------------------|
| **Encapsulation**             | Tous les attributs sont privés ; accès via getters/setters    |
| **Héritage**                  | `User` (abstract) → `Admin`, `Manager`, `Engineer`            |
| **Polymorphisme**             | `User.canCreateTask()`, `Reportable.generateReport()`         |
| **Abstraction**               | `User` est abstract, `Reportable` est une interface           |
| **Énumérations**              | `TaskStatus`, `PriorityLevel`, `TaskCategory`, `NotificationType` |
| **Exceptions personnalisées** | 7 exceptions dans le package `exceptions`                     |
| **Collections Java**          | `HashMap`, `HashSet`, `PriorityQueue`, `ArrayList`            |
| **Algorithmes (DFS)**         | `TaskManager.detectCircularDependency()`                      |
| **File I/O**                  | `FileManager` (sauvegarde / chargement)                       |
| **Tests JUnit**               | `src/test/java/fr/eseo/strms/`                                |

## 2. Fonctionnalités

- Création / suppression / mise à jour / assignation / complétion de tâches
- Gestion des dépendances entre tâches avec **détection des cycles** (DFS)
- Activation automatique en file d'attente quand les prérequis sont satisfaits
- Tableau de bord avec statistiques par statut, par utilisateur et tâches en retard
- Historique horodaté de toutes les actions sur chaque tâche
- Notifications (Console, simulation Email/SMS)
- Persistance fichier (sauvegarde / chargement)
- Gestion des permissions selon le rôle (Admin / Manager / Engineer)

## 3. Architecture du projet

```
projet-strms/
├── pom.xml                              ← Build Maven (JavaFX + JUnit 5)
├── README.md                            ← Ce fichier
├── mvnw / mvnw.cmd                      ← Maven Wrapper (Option B)
├── .mvn/wrapper/                        ← Config du Maven Wrapper
├── package-windows.bat                  ← Génère le .exe Windows (Option A)
├── docs/
│   ├── ARCHITECTURE.md                  ← Documentation technique complète
│   ├── UML.md                           ← Diagrammes (PlantUML / Mermaid)
│   ├── PRESENTATION.md                  ← Plan du support pour l'exposé
│   ├── INSTALLATION.md                  ← Guide multi-OS (Option B)
│   └── PACKAGING.md                     ← Production du .exe (Option A)
├── run.bat / run.sh                     ← Scripts de lancement (Option B)
└── src/
    ├── main/
    │   ├── java/fr/eseo/strms/
    │   │   ├── enums/                   ← TaskStatus, PriorityLevel, ...
    │   │   ├── exceptions/              ← 7 exceptions personnalisées
    │   │   ├── model/                   ← User, Admin, Manager, Engineer, Task, TaskHistoryEntry
    │   │   ├── manager/                 ← TaskManager (contrôleur central)
    │   │   ├── utils/                   ← FileManager, Reportable, ReportGenerator,
    │   │   │                             NotificationManager, Dashboard
    │   │   └── ui/                      ← Interface JavaFX (vues, dialogues)
    │   └── resources/
    │       └── css/style.css            ← Charte graphique bleu/blanc
    └── test/
        └── java/fr/eseo/strms/          ← Tests JUnit 5
```

## 4. Prérequis

- **JDK 17** ou supérieur (Eclipse Adoptium / Liberica recommandés)

> Astuce : pour vérifier votre version, lancez `java -version`.

> ✅ **Maven n'a PAS besoin d'être installé** : le projet inclut le **Maven Wrapper**
> (`mvnw` / `mvnw.cmd`) qui télécharge automatiquement la bonne version au premier
> lancement.

## 5. Comment exécuter le projet

Le projet propose **deux méthodes** de lancement, selon votre profil :

### 🅰️ Méthode A — Pour utiliser l'application sans rien installer (Windows)

Téléchargez l'archive `STRMS-1.0.0-win.zip` distribuée par votre équipe,
décompressez-la, et double-cliquez sur **`STRMS.exe`** (ou `STRMS.bat` si
Windows bloque l'`.exe`).

C'est le mode "utilisateur final" : aucun Java à installer, aucune commande à
taper. Idéal pour la démo orale du jury.

📖 **Documentation complète** : [`docs/PACKAGING.md`](docs/PACKAGING.md)
(comment générer cette archive depuis le code source).

### 🅱️ Méthode B — Pour développer et lancer depuis le code source (universelle)

Recommandée pour l'équipe. Fonctionne sur **Windows, macOS et Linux**.

**Pré-requis** : un JDK 17+ installé.

**Windows :**
```bat
mvnw.cmd javafx:run
```
Ou double-cliquer sur `run.bat`.

**macOS / Linux :**
```bash
./mvnw javafx:run
```
Ou `./run.sh`.

📖 **Documentation complète multi-OS** : [`docs/INSTALLATION.md`](docs/INSTALLATION.md)
(installation pas-à-pas du JDK, troubleshooting, etc.).

## 6. Comment exécuter les tests

**Windows :**
```bat
mvnw.cmd test
```

**macOS / Linux :**
```bash
./mvnw test
```

Les tests vérifient tous les scénarios obligatoires du cahier des charges (section 7.2) :

- ✅ Ajout réussi d'une dépendance valide
- ✅ Rejet d'une dépendance circulaire (directe et indirecte)
- ✅ Maintien de l'intégrité du graphe après rejet
- ✅ Levée des exceptions appropriées
- ✅ Suppression correcte de dépendances
- ✅ Permissions par rôle (Admin/Manager/Engineer)
- ✅ Cycle de vie des tâches (TODO → BLOCKED → IN_PROGRESS → DONE)
- ✅ Persistance fichier (round-trip save / load)

## 7. Utilisation de l'application

Au lancement, l'application est pré-chargée avec :
- 4 utilisateurs : 1 Admin (Romels), 1 Manager (Daniel), 2 Engineers (Nathan, Kylan)
- 5 tâches de démonstration avec dépendances entre elles

**Comment tester chaque fonctionnalité :**

1. **Changer d'utilisateur** : utilisez le menu déroulant en haut à droite
   pour passer entre Romels, Daniel, Nathan, Kylan et observer comment les boutons
   d'action changent selon les permissions.

2. **Créer une tâche** : cliquez sur "+ Nouvelle tâche" (visible uniquement pour Admin).

3. **Ajouter une dépendance** : sur une carte de tâche, cliquez sur "+ Dép." et
   choisissez la tâche prérequise. Essayez de créer un cycle pour voir la détection.

4. **Démarrer/Terminer une tâche** : connectez-vous en tant que Charlie ou Diana
   (Engineer), puis cliquez sur "Démarrer" puis "Terminer" sur les tâches qui
   leur sont assignées.

5. **Consulter le tableau de bord** : cliquez sur "Tableau de bord" dans la sidebar.

6. **Voir l'historique** : cliquez sur "Historique" puis sélectionnez une tâche.

7. **Sauvegarder / Charger** : cliquez sur "Rapports" puis sur les boutons
   "Sauvegarder les tâches" / "Charger des tâches".

## 8. Diagrammes UML

Voir le fichier [docs/UML.md](docs/UML.md) pour :
- Le diagramme de classes complet (PlantUML)
- Les diagrammes de séquence (création de tâche, ajout de dépendance, complétion)

Pour les visualiser : copier le code PlantUML/Mermaid dans https://www.plantuml.com/plantuml/uml
ou un outil compatible (StarUML, Draw.io, IntelliJ IDEA).

## 9. Choix techniques

Voir [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) pour la documentation technique
complète, qui justifie chaque choix de classe / structure de données / pattern.

---

> Projet réalisé dans un cadre pédagogique. Tout le code applicatif est écrit en
> Java pur (pas de framework externe au-delà de JavaFX pour l'UI et JUnit pour
> les tests).
