# Plan du support de présentation — STRMS

> Ce document est le **plan détaillé** que vous pouvez utiliser pour préparer votre support
> PowerPoint / Keynote / Google Slides. Chaque section correspond à une slide ou un
> petit groupe de slides. Les visuels suggérés sont entre crochets : `[VISUEL: ...]`.

---

## Slide 1 — Page de garde

- **Titre** : Smart Task & Resource Management System (STRMS)
- **Sous-titre** : Projet de POO Java — E3e — S6 — Spring 2026
- Noms des étudiants
- Logo ESEO
- `[VISUEL: capture d'écran de l'application — vue Kanban]`

## Slide 2 — Sommaire

1. Présentation du projet & objectifs
2. Architecture générale
3. Modèle de données (UML)
4. Démonstration des fonctionnalités
5. Algorithmes notables (détection de cycles)
6. Tests automatisés
7. Choix techniques justifiés
8. Conclusion

## Slide 3 — Présentation du projet

- **Contexte** : système simulant la gestion de tâches en environnement d'ingénierie
- **Cahier des charges** : Smart Task & Resource Management System (STRMS)
- **Stack technique** :
  - Java 17 (logique métier en Java pur)
  - JavaFX 21 (interface graphique)
  - JUnit 5 (tests)
  - Maven (build)

## Slide 4 — Objectifs pédagogiques

Tableau à 2 colonnes : "Concept" / "Mise en œuvre dans le projet"

| Concept | Mise en œuvre |
|---|---|
| Encapsulation | Tous les attributs privés |
| Héritage | `User` → Admin/Manager/Engineer |
| Polymorphisme | `canCreateTask()` redéfinie |
| Abstraction | `User` abstract, `Reportable` interface |
| Énumérations | 4 enums (TaskStatus, PriorityLevel, …) |
| Exceptions personnalisées | 7 exceptions |
| Collections | HashMap, HashSet, PriorityQueue, ArrayList |
| Tests JUnit | 20+ tests dans 2 classes |

## Slide 5 — Architecture en couches

`[VISUEL: schéma en couches Présentation → Métier → Domaine → Persistance]`

- **Présentation** : JavaFX (5 vues : Dashboard, Kanban, Users, History, Reports)
- **Métier** : `TaskManager` central + utilitaires (Dashboard, ReportGenerator, NotificationManager)
- **Domaine** : `User` (hiérarchie), `Task`, `TaskHistoryEntry`, enums
- **Persistance** : `FileManager` (format texte `.strms`)

## Slide 6 — Diagramme de classes

`[VISUEL: diagramme UML — voir docs/UML.md]`

Insister sur :
- L'héritage `User` → 3 sous-classes
- Composition Task → TaskHistoryEntry, Task → dépendances
- Lien d'utilisation TaskManager → Task / User

## Slide 7 — Modèle de tâche

`[VISUEL: zoom sur la classe Task — attributs et méthodes]`

- Encapsulation totale des données
- `Task implements Comparable<Task>` (pour PriorityQueue)
- Liste de dépendances + liste d'historique

## Slide 8 — Hiérarchie d'utilisateurs (polymorphisme)

`[VISUEL: code de Admin.canCreateTask() vs Engineer.canCreateTask()]`

Démontrer le polymorphisme :
```java
// Dans TaskManager.addTask :
if (!actor.canCreateTask()) {
    throw new InvalidRoleException(...);
}
```
Aucun `if (actor instanceof Admin)` : chaque rôle "répond pour lui-même".

## Slide 9 — Le TaskManager : contrôleur central

`[VISUEL: schéma de la façade]`

- Point d'entrée unique pour toute action
- Centralise les invariants (cycles, permissions, transitions)
- Maintient automatiquement l'historique

## Slide 10 — Diagramme de séquence : ajout de dépendance

`[VISUEL: diagramme de séquence — voir docs/UML.md]`

Mettre en avant :
1. Validation des permissions
2. Détection DFS du cycle
3. Levée d'exception ou enregistrement
4. Mise à jour automatique du statut (BLOCKED si nécessaire)

## Slide 11 — Détection de cycle (DFS)

`[VISUEL: code de detectCircularDependency() + schéma graphe]`

```
Task A → Task B → Task C
            ↑________|  ← Cycle si on ajoute C → A
```

Algorithme : on part de `dependsOn` et on remonte le graphe ; si on tombe sur `task`, c'est un cycle.

**Complexité : O(V + E)**.

## Slide 12 — Cycle de vie d'une tâche

`[VISUEL: state diagram — TODO ↔ BLOCKED → IN_PROGRESS → DONE]`

Insister sur :
- DONE est terminal
- Une tâche se débloque automatiquement quand toutes ses dépendances sont DONE

## Slide 13 — Démonstration : Tableau Kanban

`[VISUEL: capture d'écran de l'application — KanbanView]`

Montrer :
- 4 colonnes (TODO / BLOCKED / IN_PROGRESS / DONE)
- Cartes colorées par priorité
- Boutons d'action contextuels selon le rôle

## Slide 14 — Démonstration : Tableau de bord

`[VISUEL: capture d'écran de DashboardView]`

KPIs visibles :
- Compteurs par statut
- Tâches par ingénieur
- Tâches en retard

## Slide 15 — Démonstration : changement d'utilisateur

`[VISUEL: 2 captures — interface vue par Admin vs Engineer]`

Démontrer en live le polymorphisme :
- Connecté en Admin → bouton "Supprimer" visible
- Connecté en Engineer → uniquement "Démarrer / Terminer"

## Slide 16 — Démonstration : détection de cycle en direct

Tester depuis l'UI :
- Ajouter B → A puis C → B
- Tenter A → C
- → message d'erreur, graphe inchangé

## Slide 17 — Persistance fichier

`[VISUEL: extrait d'un fichier .strms]`

```
TASK;T-001;Concevoir BDD;...;HIGH;DONE;RESEARCH;2026-05-15;U-ENG-1
DEP;T-002;T-001
HIST;T-001;2026-05-04T10:23:45;CREATION;Alice;Tâche créée
```

Format texte, lisible, robuste à l'évolution.

## Slide 18 — Tests JUnit

`[VISUEL: capture du résultat 'mvn test']`

20+ tests couvrant :
- Permissions (Admin / Manager / Engineer)
- Cycle de vie (TODO → DONE)
- Dépendances (ajout, suppression, cycles directs/indirects)
- Persistance (round-trip save/load)

## Slide 19 — Choix techniques justifiés

Tableau structures de données / justification :

| Structure | Pourquoi |
|---|---|
| `HashMap<String, Task>` | Lookup O(1) par id |
| `HashSet<Task>` | Suivi sans doublons |
| `PriorityQueue<Task>` | Sélection automatique de la plus prioritaire |
| `ArrayList<TaskHistoryEntry>` | Ordre chronologique préservé |

## Slide 20 — Difficultés rencontrées

Exemples (à adapter selon votre vécu) :
- Synchronisation entre `readyQueue` et `inProgressTasks` lors des transitions
- Gestion des cycles dans le graphe de dépendances
- Apprentissage de JavaFX (layouts, CSS)

## Slide 21 — Améliorations possibles

- Migration vers une base de données (SQLite) au lieu du fichier texte
- Authentification réelle des utilisateurs (login + mot de passe)
- Notifications réelles (Email via SMTP, SMS via API)
- Mode multi-utilisateur (client/serveur)
- Tests UI (TestFX)

## Slide 22 — Démonstration finale & Q/R

- Lancer `mvn javafx:run`
- Démo libre selon les questions du jury
- Merci de votre attention !

---

## Annexe — Captures d'écran à préparer

À insérer dans la présentation :

1. Vue **Kanban** avec les 4 colonnes peuplées
2. Vue **Dashboard** avec les KPI
3. Vue **Historique** avec une tâche sélectionnée
4. Vue **Rapports** avec le rapport généré
5. Boîte de dialogue **CreateTaskDialog**
6. **Alert** d'erreur lors d'une tentative de cycle
7. Sortie console montrant les notifications
8. Résultat de `mvn test` (tests verts)
9. Extrait du **diagramme UML**
10. Extrait de code de la **détection de cycle**
