# Diagrammes UML — STRMS

Les diagrammes ci-dessous sont en syntaxe **PlantUML** (copier-coller dans
<https://www.plantuml.com/plantuml/uml/> pour les visualiser) et **Mermaid**
(rendu automatique sur GitHub / GitLab).

---

## 1. Diagramme de classes (PlantUML)

```plantuml
@startuml STRMS-Class-Diagram
skinparam classAttributeIconSize 0
skinparam shadowing false
skinparam class {
    BackgroundColor #FFFFFF
    BorderColor #0052CC
    ArrowColor #172B4D
}

' ====== Enumerations ======
enum TaskStatus {
    TODO
    BLOCKED
    IN_PROGRESS
    DONE
}

enum PriorityLevel {
    LOW
    MEDIUM
    HIGH
    CRITICAL
}

enum TaskCategory {
    BUGFIX
    FEATURE
    DOCUMENTATION
    RESEARCH
}

enum NotificationType {
    EMAIL
    SMS
    CONSOLE
}

' ====== User hierarchy ======
abstract class User {
    - id : String
    - name : String
    - email : String
    + getRole() : String {abstract}
    + canCreateTask() : boolean {abstract}
    + canDeleteTask() : boolean {abstract}
    + canAssignTask() : boolean {abstract}
    + canExecuteTask() : boolean {abstract}
    + canGenerateReport() : boolean {abstract}
}

class Admin {
    + canCreateTask() : boolean
    + canDeleteTask() : boolean
    + canAssignTask() : boolean
}

class Manager {
    + canAssignTask() : boolean
    + canGenerateReport() : boolean
}

class Engineer {
    + canExecuteTask() : boolean
}

User <|-- Admin
User <|-- Manager
User <|-- Engineer

' ====== Domain model ======
class Task {
    - id : String
    - title : String
    - description : String
    - priority : PriorityLevel
    - status : TaskStatus
    - category : TaskCategory
    - deadline : LocalDate
    - assignedEngineer : Engineer
    - dependencies : List<Task>
    - history : List<TaskHistoryEntry>
    + updateStatus(s : TaskStatus) : void
    + changePriority(p : PriorityLevel) : void
    + markAsDone() : void
    + addDependency(t : Task) : void
    + removeDependency(t : Task) : boolean
    + areDependenciesCompleted() : boolean
    + addHistoryEntry(e : TaskHistoryEntry) : void
    + compareTo(other : Task) : int
}

class TaskHistoryEntry <<final, immutable>> {
    - action : String
    - performedBy : String
    - timestamp : LocalDateTime
    - description : String
}

Task "1" *-- "0..*" Task : depends on
Task "1" *-- "0..*" TaskHistoryEntry : history
Task ..> TaskStatus
Task ..> PriorityLevel
Task ..> TaskCategory
Task "0..*" --> "0..1" Engineer : assigned

' ====== Controller ======
class TaskManager {
    - tasks : Map<String, Task>
    - users : Map<String, User>
    - inProgressTasks : Set<Task>
    - readyQueue : PriorityQueue<Task>
    + addTask(t : Task, actor : User) : void
    + deleteTask(id : String, actor : User) : void
    + addDependency(...)
    + removeDependency(...)
    + assignTask(id, engId, actor)
    + startTask(id, actor)
    + completeTask(id, actor)
    + detectCircularDependency(...) : boolean
    + findTask(id) : Task
}

TaskManager "1" o-- "0..*" Task
TaskManager "1" o-- "0..*" User

' ====== Utilities ======
interface Reportable {
    + generateReport() : String
}

class ReportGenerator {
    + generateReport() : String
}

class NotificationManager {
    + notify(u : User, msg : String, t : NotificationType)
}

class Dashboard {
    + countTasksByStatus() : Map<TaskStatus, Integer>
    + countTasksByUser() : Map<String, Integer>
    + countOverdueTasks() : int
}

class FileManager {
    + saveTasksToFile(tm, path)
    + loadTasksFromFile(tm, path)
    + writeReport(content, path)
}

Reportable <|.. ReportGenerator
ReportGenerator --> TaskManager
Dashboard --> TaskManager
FileManager ..> TaskManager

' ====== Exceptions (omitted detail; all extend Exception) ======
class CircularDependencyException
class DependencyNotCompletedException
class TaskNotFoundException
class InvalidRoleException
class FilePersistenceException
class DuplicateTaskException
class InvalidTaskStateException

@enduml
```

### Aperçu Mermaid (rendu GitHub)

```mermaid
classDiagram
    class User {
        <<abstract>>
        -id: String
        -name: String
        -email: String
        +canCreateTask()* boolean
        +canDeleteTask()* boolean
        +canAssignTask()* boolean
        +canExecuteTask()* boolean
        +canGenerateReport()* boolean
    }
    class Admin
    class Manager
    class Engineer
    User <|-- Admin
    User <|-- Manager
    User <|-- Engineer

    class Task {
        -id: String
        -title: String
        -priority: PriorityLevel
        -status: TaskStatus
        -dependencies: List~Task~
        -history: List~TaskHistoryEntry~
        +updateStatus(s)
        +addDependency(t)
        +areDependenciesCompleted() boolean
    }
    class TaskHistoryEntry {
        <<immutable>>
        -action
        -performedBy
        -timestamp
    }
    Task "1" *-- "0..*" Task : depends on
    Task "1" *-- "0..*" TaskHistoryEntry

    class TaskManager {
        -tasks: Map
        -users: Map
        -inProgressTasks: Set
        -readyQueue: PriorityQueue
        +addTask(t, actor)
        +addDependency(id, depId, actor)
        +completeTask(id, actor)
        +detectCircularDependency(t, depOn) boolean
    }
    TaskManager o-- Task
    TaskManager o-- User

    class Reportable {
        <<interface>>
        +generateReport() String
    }
    class ReportGenerator
    Reportable <|.. ReportGenerator

    class Dashboard
    class FileManager
    class NotificationManager
```

---

## 2. Diagramme de séquence — Création de tâche

```plantuml
@startuml
actor Alice as "Alice (Admin)"
participant UI as "KanbanView"
participant TM as "TaskManager"
participant T as "Task"
participant H as "TaskHistoryEntry"

Alice -> UI : Clique sur '+ Nouvelle tâche'
UI -> UI : Ouvre CreateTaskDialog
Alice -> UI : Saisit les champs et valide
UI -> T : new Task(...)
UI -> TM : addTask(task, alice)
TM -> TM : ensurePermission(alice.canCreateTask())
TM -> TM : tasks.put(id, task)
TM -> TM : readyQueue.offer(task)
TM -> H : new TaskHistoryEntry("CREATION", "Alice", ...)
TM -> T : addHistoryEntry(entry)
TM --> UI : (ok)
UI -> UI : refreshAll()
@enduml
```

---

## 3. Diagramme de séquence — Ajout de dépendance avec détection de cycle

```plantuml
@startuml
actor Alice as "Alice (Admin)"
participant UI as "KanbanView"
participant TM as "TaskManager"
participant T2 as "Task T-2"
participant T1 as "Task T-1"

Alice -> UI : Clique '+ Dép.' sur T-2
UI -> UI : Ouvre AddDependencyDialog
Alice -> UI : Choisit T-1 comme prérequis
UI -> TM : addDependency("T-2", "T-1", alice)

TM -> TM : task = requireTask("T-2")
TM -> TM : dependsOn = requireTask("T-1")

alt cycle direct (task == dependsOn)
    TM --> UI : throw CircularDependencyException
else détection DFS
    TM -> TM : detectCircularDependency(task, dependsOn)
    alt cycle détecté
        TM -> T2 : addHistoryEntry("DEPENDENCY_REJECTED", ...)
        TM --> UI : throw CircularDependencyException
        UI -> Alice : Alert "Dépendance circulaire"
    else pas de cycle
        TM -> T2 : addDependency(T1)
        TM -> T2 : updateStatus(BLOCKED) [si T1 non DONE]
        TM -> T2 : addHistoryEntry("DEPENDENCY_ADDED", ...)
        TM --> UI : (ok)
    end
end
@enduml
```

---

## 4. Diagramme de séquence — Complétion d'une tâche

```plantuml
@startuml
actor Charlie as "Charlie (Engineer)"
participant UI as "KanbanView"
participant TM as "TaskManager"
participant T1 as "Task T-1"
participant T2 as "Task T-2"

Charlie -> UI : Clique 'Terminer' sur T-1
UI -> TM : completeTask("T-1", charlie)

TM -> TM : ensurePermission(charlie.canExecuteTask())
TM -> TM : verify charlie == T1.assignedEngineer
TM -> T1 : markAsDone()  // status -> DONE
TM -> TM : inProgressTasks.remove(T1)
TM -> T1 : addHistoryEntry("STATUS_CHANGE", ...)

TM -> TM : pour chaque tâche T_other qui dépend de T1
TM -> T2 : refreshTaskReadiness()
note right
  Si toutes les dépendances de T2 sont DONE,
  T2 passe BLOCKED -> TODO et entre dans la
  readyQueue.
end note

TM --> UI : (ok)
UI -> UI : refreshAll()
@enduml
```

---

## 5. Diagramme de cycle de vie d'une tâche

```mermaid
stateDiagram-v2
    [*] --> TODO: addTask
    TODO --> BLOCKED: addDependency (dep non DONE)
    BLOCKED --> TODO: refreshTaskReadiness (dep DONE)
    TODO --> IN_PROGRESS: assignTask / startTask
    BLOCKED --> IN_PROGRESS: assignTask (dep DONE)
    IN_PROGRESS --> DONE: completeTask
    DONE --> [*]
```
