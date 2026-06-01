# Documentation technique — STRMS

Ce document décrit l'architecture du projet, les choix techniques et les responsabilités de chaque composant.

## 1. Vue d'ensemble

L'application STRMS est structurée selon un patron en couches :

```
┌──────────────────────────────────────────────────────────┐
│                    Couche Présentation                   │
│                  (JavaFX — package ui)                   │
│   MainApp · MainView · DashboardView · KanbanView · ...  │
└──────────────────────────────────────────────────────────┘
                          ↓ utilise
┌──────────────────────────────────────────────────────────┐
│                    Couche Métier                         │
│              (package manager + utils)                   │
│   TaskManager · NotificationManager · Dashboard · ...    │
└──────────────────────────────────────────────────────────┘
                          ↓ manipule
┌──────────────────────────────────────────────────────────┐
│                    Couche Domaine                        │
│             (packages model + enums)                     │
│   User · Admin · Manager · Engineer · Task · ...         │
└──────────────────────────────────────────────────────────┘
                          ↓ persiste via
┌──────────────────────────────────────────────────────────┐
│                Couche Persistance                        │
│              (FileManager — package utils)               │
└──────────────────────────────────────────────────────────┘
```

## 2. Responsabilités des classes principales

### 2.1 Couche domaine

| Classe              | Rôle                                                                 |
|---------------------|----------------------------------------------------------------------|
| `User` (abstract)   | Définit l'API commune des rôles : id, nom, email, permissions.       |
| `Admin`             | Tous les droits sauf l'exécution de tâches.                          |
| `Manager`           | Peut assigner des tâches et générer des rapports.                    |
| `Engineer`          | Peut exécuter (démarrer / terminer) ses tâches assignées.            |
| `Task`              | Encapsule une tâche : id, statut, priorité, deadline, dépendances, historique. Implémente `Comparable<Task>` pour le tri en `PriorityQueue`. |
| `TaskHistoryEntry`  | **Immuable** : enregistre une action (timestamp, acteur, description). |

### 2.2 Couche métier

| Classe                | Rôle                                                                 |
|-----------------------|----------------------------------------------------------------------|
| `TaskManager`         | Contrôleur central : applique les règles métier (permissions, cycles, transitions). |
| `Dashboard`           | Calcule les statistiques agrégées sur les tâches.                    |
| `ReportGenerator`     | Génère un rapport texte (implémente `Reportable`).                   |
| `NotificationManager` | Envoie des notifications (Console, simulation Email/SMS).            |

### 2.3 Persistance

| Classe        | Rôle                                                  |
|---------------|-------------------------------------------------------|
| `FileManager` | Sérialise/désérialise les tâches en CSV-like (`.strms`). |

## 3. Choix techniques

### 3.1 Pourquoi un `TaskManager` central ?

Le cahier des charges décrit un contrôleur central. C'est aussi un patron *Façade* :
le `TaskManager` cache la complexité (validation, cycle, historique, transitions)
derrière une API simple (`addTask`, `assignTask`, `completeTask`...).

**Bénéfices** :
- Les invariants (pas de cycle, pas de duplicate id, permissions correctes) sont **garantis en un seul endroit**.
- L'UI ne touche jamais directement les modèles : elle passe toujours par le manager.
- L'historique est **automatiquement** ajouté pour chaque action.

### 3.2 Choix des structures de données

| Structure                       | Justification                                                              |
|---------------------------------|----------------------------------------------------------------------------|
| `HashMap<String, Task>`         | Lookup en **O(1)** par identifiant — la recherche est l'opération la plus fréquente. |
| `HashMap<String, User>`         | Idem pour les utilisateurs.                                                |
| `HashSet<Task>`                 | Suivi des tâches en cours **sans doublons** + appartenance en O(1).        |
| `PriorityQueue<Task>`           | Sélection automatique de la tâche **la plus prioritaire** (heap min).      |
| `ArrayList<TaskHistoryEntry>`   | Historique **ordonné** (chronologique) avec accès indexé.                  |
| `ArrayList<Task>` (deps)        | Petite liste, ordre stable, itération fréquente.                           |

### 3.3 Détection de cycles

Algorithme : **Depth-First Search (DFS)** sur le graphe des dépendances.

Question : ajouter "A dépend de B" crée-t-il un cycle ?
→ équivaut à se demander si `B` dépend (directement ou indirectement) de `A`.

On part donc de `B` et on suit les dépendances, en collectant les nœuds visités
dans un `HashSet` pour éviter les boucles infinies. Si on tombe sur `A`, il y a cycle.

**Complexité** : O(V + E) où V = nombre de tâches, E = nombre de dépendances.

```java
Set<Task> visited = new HashSet<>();
Deque<Task> stack = new ArrayDeque<>();
stack.push(dependsOn);
while (!stack.isEmpty()) {
    Task current = stack.pop();
    if (current.equals(task)) return true;          // cycle !
    if (visited.add(current)) {
        for (Task dep : current.getDependencies()) stack.push(dep);
    }
}
return false;
```

### 3.4 Polymorphisme via permissions

Chaque sous-classe de `User` redéfinit les méthodes `canCreateTask()`, `canDeleteTask()`,
`canAssignTask()`, `canExecuteTask()`, `canGenerateReport()`. Le `TaskManager`
n'a pas besoin d'un `if/else` ou d'un `switch` sur le rôle :
il appelle simplement `actor.canCreateTask()`, et **chaque rôle répond pour lui-même**.

C'est un exemple direct de polymorphisme : un seul code dans le manager, comportement différent selon l'objet réel.

### 3.5 Immuabilité de l'historique

`TaskHistoryEntry` est `final` avec **uniquement des attributs final** et **aucun setter**.
Cela garantit qu'une fois enregistré, un événement d'historique **ne peut pas être altéré**,
ce qui est exigé par le cahier des charges (auditabilité).

### 3.6 Format de fichier `.strms`

Format texte ligne par ligne (CSV-like avec `;` comme séparateur). Trois types de lignes :

```
TASK;id;titre;description;priorité;statut;catégorie;deadline;ingénieurId
DEP;taskId;dependsOnId
HIST;taskId;timestamp;action;performedBy;description
```

**Pourquoi pas de la sérialisation Java native ?**
- Le format texte est lisible humainement (debug facile).
- Évite les problèmes de versionning binaire en cas d'évolution du modèle.
- Pédagogiquement plus parlant.

Les `;` à l'intérieur des champs sont échappés en `\;` pour éviter les conflits.

### 3.7 JavaFX : approche programmatique (pas de FXML)

Toutes les vues sont construites **par code Java** (pas de FXML).
Avantages pour un projet pédagogique :
- Une seule technologie à maîtriser (Java pur)
- Refactoring/recherche plus simples avec un IDE
- Pas de duplication entre fichiers FXML et controllers

## 4. Cycle de vie d'une tâche

```
                 addTask
                    │
                    ↓
┌─────────────┐   addDep (avec dep non DONE)   ┌─────────────┐
│    TODO     │ ─────────────────────────────→ │   BLOCKED   │
└──────┬──────┘                                 └──────┬──────┘
       │                                                │
       │ assignTask  /                                  │  dep terminée
       │ startTask                                      │  (refresh)
       ↓                                                ↓
┌─────────────┐                                 ┌─────────────┐
│ IN_PROGRESS │ ←───────────────────────────── │    TODO     │
└──────┬──────┘                                 └─────────────┘
       │ completeTask
       ↓
┌─────────────┐
│    DONE     │  (terminal)
└─────────────┘
```

## 5. Gestion des erreurs

7 exceptions personnalisées, toutes **checked** (héritent de `Exception`) :

| Exception                          | Quand ?                                                  |
|-----------------------------------|----------------------------------------------------------|
| `CircularDependencyException`     | Ajout d'une dépendance créerait un cycle                 |
| `DependencyNotCompletedException` | Démarrage / complétion avec dépendance non terminée      |
| `TaskNotFoundException`           | Identifiant de tâche inconnu                             |
| `InvalidRoleException`            | Action interdite par le rôle de l'utilisateur            |
| `FilePersistenceException`        | Erreur d'I/O fichier                                     |
| `DuplicateTaskException`          | Identifiant de tâche déjà utilisé                        |
| `InvalidTaskStateException`       | Transition de statut invalide                            |

L'UI traite ces exceptions via des `Alert` dans la `KanbanView` (méthode `showError`).

## 6. Tests

Deux classes de tests :

- `TaskManagerTest` : 18 tests couvrant les scénarios obligatoires + cycle de vie + permissions.
- `FilePersistenceTest` : tests d'intégration save/load.

**Tous les scénarios obligatoires** du cahier des charges (section 7.2) sont couverts.
