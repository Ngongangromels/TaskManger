package fr.eseo.strms.manager;

import fr.eseo.strms.enums.PriorityLevel;
import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.exceptions.*;
import fr.eseo.strms.model.*;

import java.util.*;

/**
 * Contrôleur central du système STRMS.
 *
 * Le TaskManager est responsable de :
 * - La gestion du cycle de vie des tâches (création, suppression, mise à jour, assignation, complétion).
 * - La gestion du graphe des dépendances entre tâches (avec détection de cycles par DFS).
 * - L'enregistrement automatique de l'historique pour chaque action significative.
 * - La vérification des permissions selon le rôle de l'utilisateur (RBAC).
 * - La fourniture de structures de données prêtes à être consultées par l'UI/dashboard.
 *
 * Structures de données utilisées (cf. cahier des charges, section 5) :
 *   - HashMap&lt;String, Task&gt;  : stockage de toutes les tâches (lookup O(1) par id).
 *   - HashMap&lt;String, User&gt;  : stockage des utilisateurs (lookup O(1) par id).
 *   - HashSet&lt;Task&gt;          : suivi des tâches en cours (sans doublons).
 *   - PriorityQueue&lt;Task&gt;    : ordonnancement des tâches prêtes par priorité.
 *   - ArrayList                    : utilisé dans Task pour l'historique et les dépendances.
 *
 * Aucune persistance n'est faite ici directement : le FileManager est utilisé en complément
 * (saveTasksToFile / loadTasksFromFile).
 */
public class TaskManager {

    private final Map<String, Task> tasks;
    private final Map<String, User> users;
    private final Set<Task> inProgressTasks;
    private final PriorityQueue<Task> readyQueue;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.users = new HashMap<>();
        this.inProgressTasks = new HashSet<>();
        this.readyQueue = new PriorityQueue<>();
    }

    // ===================== Gestion des utilisateurs =====================

    /** Enregistre un utilisateur dans le système. */
    public void registerUser(User user) {
        if (user != null) {
            users.put(user.getId(), user);
        }
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    // ===================== Création / suppression de tâches =====================

    /**
     * Ajoute une nouvelle tâche dans le système.
     *
     * Étapes :
     * 1. Vérifier que l'utilisateur a la permission de créer une tâche.
     * 2. Vérifier que l'identifiant n'est pas déjà utilisé.
     * 3. Insérer la tâche dans la map.
     * 4. Si elle n'a pas de dépendance, la mettre dans la file de priorité.
     * 5. Enregistrer une entrée d'historique de création.
     */
    public void addTask(Task task, User actor)
            throws InvalidRoleException, DuplicateTaskException {

        ensurePermission(actor, actor.canCreateTask(), "créer une tâche");
        if (task == null) {
            throw new IllegalArgumentException("La tâche ne peut pas être null.");
        }
        if (tasks.containsKey(task.getId())) {
            throw new DuplicateTaskException(
                    "Une tâche avec l'identifiant '" + task.getId() + "' existe déjà.");
        }

        tasks.put(task.getId(), task);

        // La tâche est immédiatement prête s'il n'y a aucune dépendance
        if (task.areDependenciesCompleted() && task.getStatus() == TaskStatus.TODO) {
            readyQueue.offer(task);
        }

        task.addHistoryEntry(new TaskHistoryEntry(
                "CREATION", actor.getName(),
                "Tâche '" + task.getTitle() + "' créée par " + actor.getRole()));
    }

    /**
     * Supprime une tâche du système.
     */
    public void deleteTask(String taskId, User actor)
            throws InvalidRoleException, TaskNotFoundException {

        ensurePermission(actor, actor.canDeleteTask(), "supprimer une tâche");
        Task task = requireTask(taskId);

        tasks.remove(taskId);
        readyQueue.remove(task);
        inProgressTasks.remove(task);

        // Retire la tâche des dépendances de toutes les autres tâches
        for (Task other : tasks.values()) {
            if (other.removeDependency(task)) {
                other.addHistoryEntry(new TaskHistoryEntry(
                        "DEPENDENCY_REMOVED", actor.getName(),
                        "La dépendance vers " + task.getId() + " a été supprimée (tâche supprimée)."));
                refreshTaskReadiness(other);
            }
        }
    }

    // ===================== Recherche =====================

    /** Recherche une tâche par son identifiant. */
    public Task findTask(String taskId) throws TaskNotFoundException {
        return requireTask(taskId);
    }

    public Collection<Task> getAllTasks() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    public Set<Task> getInProgressTasks() {
        return Collections.unmodifiableSet(inProgressTasks);
    }

    // ===================== Dépendances =====================

    /**
     * Ajoute une dépendance : task -> dependsOn.
     *
     * Vérifie :
     *  - Les deux tâches existent.
     *  - Une tâche ne dépend pas d'elle-même.
     *  - Aucune dépendance circulaire n'est créée.
     *
     * Si la tâche dépendante n'est pas DONE, la tâche cible passe en BLOCKED.
     */
    public void addDependency(String taskId, String dependsOnId, User actor)
            throws TaskNotFoundException, CircularDependencyException, InvalidTaskStateException {

        Task task = requireTask(taskId);
        Task dependsOn = requireTask(dependsOnId);

        if (task.equals(dependsOn)) {
            throw new CircularDependencyException(
                    "Une tâche ne peut pas dépendre d'elle-même : " + taskId);
        }

        // Détection de cycle : on simule l'ajout et on lance un DFS depuis dependsOn
        if (detectCircularDependency(task, dependsOn)) {
            // Log dans l'historique de la tentative rejetée
            task.addHistoryEntry(new TaskHistoryEntry(
                    "DEPENDENCY_REJECTED",
                    actor != null ? actor.getName() : "système",
                    "Tentative d'ajout d'une dépendance vers " + dependsOnId
                            + " rejetée : cela créerait un cycle."));
            throw new CircularDependencyException(
                    "Dépendance circulaire détectée : ajouter "
                            + taskId + " -> " + dependsOnId + " créerait un cycle.");
        }

        task.addDependency(dependsOn);

        // Si la dépendance n'est pas terminée, on bloque la tâche
        if (dependsOn.getStatus() != TaskStatus.DONE
                && task.getStatus() != TaskStatus.IN_PROGRESS
                && task.getStatus() != TaskStatus.DONE) {
            task.updateStatus(TaskStatus.BLOCKED);
            readyQueue.remove(task);
        }

        task.addHistoryEntry(new TaskHistoryEntry(
                "DEPENDENCY_ADDED",
                actor != null ? actor.getName() : "système",
                "La tâche dépend désormais de " + dependsOnId));
    }

    /**
     * Détection de cycle par parcours en profondeur (DFS).
     *
     * Question : si on ajoute "task dépend de dependsOn", crée-t-on un cycle ?
     * Cycle <=> dependsOn dépend (directement ou indirectement) de task.
     * On part donc de dependsOn et on remonte ses dépendances ; si on tombe sur task,
     * il y a cycle.
     */
    public boolean detectCircularDependency(Task task, Task dependsOn) {
        Set<Task> visited = new HashSet<>();
        Deque<Task> stack = new ArrayDeque<>();
        stack.push(dependsOn);

        while (!stack.isEmpty()) {
            Task current = stack.pop();
            if (current.equals(task)) {
                return true;
            }
            if (visited.add(current)) {
                for (Task dep : current.getDependencies()) {
                    stack.push(dep);
                }
            }
        }
        return false;
    }

    /**
     * Retire une dépendance et met à jour l'état de la tâche en conséquence.
     */
    public void removeDependency(String taskId, String dependsOnId, User actor)
            throws TaskNotFoundException {

        Task task = requireTask(taskId);
        Task dependsOn = requireTask(dependsOnId);

        if (task.removeDependency(dependsOn)) {
            task.addHistoryEntry(new TaskHistoryEntry(
                    "DEPENDENCY_REMOVED",
                    actor != null ? actor.getName() : "système",
                    "Dépendance vers " + dependsOnId + " retirée."));
            refreshTaskReadiness(task);
        }
    }

    // ===================== Assignation / exécution =====================

    /**
     * Assigne une tâche à un ingénieur.
     * Si toutes les dépendances sont satisfaites, la tâche passe en IN_PROGRESS.
     * Sinon elle reste BLOCKED.
     */
    public void assignTask(String taskId, String engineerId, User actor)
            throws TaskNotFoundException, InvalidRoleException, InvalidTaskStateException {

        ensurePermission(actor, actor.canAssignTask(), "assigner une tâche");
        Task task = requireTask(taskId);

        User user = users.get(engineerId);
        if (!(user instanceof Engineer engineer)) {
            throw new InvalidRoleException("L'utilisateur " + engineerId + " n'est pas un ingénieur.");
        }

        task.setAssignedEngineer(engineer);
        task.addHistoryEntry(new TaskHistoryEntry(
                "ASSIGNMENT", actor.getName(),
                "Tâche assignée à " + engineer.getName()));

        if (task.areDependenciesCompleted() && task.getStatus() != TaskStatus.DONE) {
            task.updateStatus(TaskStatus.IN_PROGRESS);
            readyQueue.remove(task);
            inProgressTasks.add(task);
        } else if (task.getStatus() != TaskStatus.DONE) {
            task.updateStatus(TaskStatus.BLOCKED);
        }
    }

    /**
     * L'ingénieur assigné démarre la tâche : passage explicite à IN_PROGRESS.
     */
    public void startTask(String taskId, User actor)
            throws TaskNotFoundException, InvalidRoleException,
            InvalidTaskStateException, DependencyNotCompletedException {

        ensurePermission(actor, actor.canExecuteTask(), "exécuter une tâche");
        Task task = requireTask(taskId);

        if (!(actor instanceof Engineer) || !actor.equals(task.getAssignedEngineer())) {
            throw new InvalidRoleException(
                    "Seul l'ingénieur assigné peut démarrer cette tâche.");
        }
        if (!task.areDependenciesCompleted()) {
            // Trace dans l'historique le rejet
            task.addHistoryEntry(new TaskHistoryEntry(
                    "START_REJECTED", actor.getName(),
                    "Tentative de démarrage rejetée : dépendances non terminées."));
            throw new DependencyNotCompletedException(
                    "Impossible de démarrer la tâche " + taskId
                            + " : toutes les dépendances ne sont pas terminées.");
        }

        task.updateStatus(TaskStatus.IN_PROGRESS);
        readyQueue.remove(task);
        inProgressTasks.add(task);
        task.addHistoryEntry(new TaskHistoryEntry(
                "STATUS_CHANGE", actor.getName(),
                "Statut changé à IN_PROGRESS"));
    }

    /**
     * Termine une tâche : passage explicite à DONE.
     *
     * Règle métier : seuls l'Admin et le Manager peuvent clôturer une tâche.
     * Si un Engineer tente de le faire, une InvalidRoleException est levée.
     *
     * Toutes les tâches qui dépendaient de celle-ci sont ré-évaluées
     * (elles peuvent passer de BLOCKED à TODO/READY).
     */
    public void completeTask(String taskId, User actor)
            throws TaskNotFoundException, InvalidRoleException,
            InvalidTaskStateException, DependencyNotCompletedException {

        ensurePermission(actor, actor.canCompleteTask(), "compléter une tâche");
        Task task = requireTask(taskId);

        if (!task.areDependenciesCompleted()) {
            throw new DependencyNotCompletedException(
                    "Toutes les dépendances ne sont pas terminées.");
        }

        task.markAsDone();
        inProgressTasks.remove(task);
        readyQueue.remove(task);

        task.addHistoryEntry(new TaskHistoryEntry(
                "STATUS_CHANGE", actor.getName(),
                "Tâche terminée (DONE) par " + actor.getRole()));

        // Réévaluer les tâches qui dépendaient de celle-ci
        for (Task other : tasks.values()) {
            if (other.getDependencies().contains(task)) {
                refreshTaskReadiness(other);
            }
        }
    }

    /**
     * Met à jour la priorité d'une tâche.
     * On retire/réinsère la tâche dans la file pour que le tri reste cohérent.
     */
    public void updateTaskPriority(String taskId, PriorityLevel newPriority, User actor)
            throws TaskNotFoundException, InvalidRoleException {

        ensurePermission(actor, actor.canCreateTask() || actor.canAssignTask(),
                "modifier la priorité d'une tâche");
        Task task = requireTask(taskId);

        boolean inQueue = readyQueue.remove(task);
        task.changePriority(newPriority);
        if (inQueue) {
            readyQueue.offer(task);
        }
        task.addHistoryEntry(new TaskHistoryEntry(
                "PRIORITY_CHANGE", actor.getName(),
                "Priorité modifiée à " + newPriority.getLabel()));
    }

    /**
     * Met à jour la description d'une tâche.
     */
    public void updateTaskDescription(String taskId, String newDescription, User actor)
            throws TaskNotFoundException, InvalidRoleException {

        ensurePermission(actor, actor.canCreateTask() || actor.canAssignTask(),
                "modifier la description d'une tâche");
        Task task = requireTask(taskId);
        task.updateDescription(newDescription);
        task.addHistoryEntry(new TaskHistoryEntry(
                "DESCRIPTION_CHANGE", actor.getName(),
                "Description mise à jour."));
    }

    // ===================== Affichage / consultations =====================

    /** Affiche dans la console toutes les tâches en cours. */
    public void printInProgressTasks() {
        System.out.println("=== Tâches en cours (" + inProgressTasks.size() + ") ===");
        for (Task t : inProgressTasks) {
            System.out.println(" - " + t.displayTask());
        }
    }

    /** Retourne la prochaine tâche à exécuter selon la priorité (ou null si la file est vide). */
    public Task peekNextReadyTask() {
        return readyQueue.peek();
    }

    public int getReadyQueueSize() {
        return readyQueue.size();
    }

    /**
     * Insère directement une tâche déjà construite (utilisé uniquement par le FileManager
     * lors de la restauration depuis fichier). Pas de vérification de permissions
     * ni d'historique : la tâche est supposée provenir d'un état déjà validé.
     */
    public void injectTaskFromFile(Task task) {
        if (task == null) return;
        tasks.put(task.getId(), task);
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            inProgressTasks.add(task);
        } else if (task.getStatus() == TaskStatus.TODO && task.areDependenciesCompleted()) {
            readyQueue.offer(task);
        }
    }

    // ===================== Helpers privés =====================

    /**
     * Recalcule le statut d'une tâche en fonction de ses dépendances.
     * - Si elle est BLOCKED et que toutes les dépendances sont terminées, on la repasse à TODO
     *   et on la place dans la file ready.
     * - Si elle est TODO mais qu'une dépendance n'est pas terminée, on la passe à BLOCKED.
     */
    private void refreshTaskReadiness(Task task) {
        try {
            if (task.getStatus() == TaskStatus.DONE
                    || task.getStatus() == TaskStatus.IN_PROGRESS) {
                return;
            }
            if (task.areDependenciesCompleted()) {
                if (task.getStatus() == TaskStatus.BLOCKED) {
                    task.updateStatus(TaskStatus.TODO);
                    task.addHistoryEntry(new TaskHistoryEntry(
                            "UNBLOCKED", "système",
                            "Toutes les dépendances sont terminées, la tâche est débloquée."));
                }
                if (!readyQueue.contains(task)) {
                    readyQueue.offer(task);
                }
            } else {
                if (task.getStatus() != TaskStatus.BLOCKED) {
                    task.updateStatus(TaskStatus.BLOCKED);
                    task.addHistoryEntry(new TaskHistoryEntry(
                            "BLOCKED", "système",
                            "La tâche est bloquée par une dépendance non terminée."));
                }
                readyQueue.remove(task);
            }
        } catch (InvalidTaskStateException ex) {
            // Si la tâche est DONE, on ignore : pas de transition nécessaire.
        }
    }

    /** Récupère une tâche par id ou lève TaskNotFoundException. */
    private Task requireTask(String taskId) throws TaskNotFoundException {
        Task t = tasks.get(taskId);
        if (t == null) {
            throw new TaskNotFoundException("Aucune tâche trouvée avec l'identifiant : " + taskId);
        }
        return t;
    }

    /** Lève InvalidRoleException si la condition n'est pas remplie. */
    private void ensurePermission(User actor, boolean allowed, String operation)
            throws InvalidRoleException {
        if (actor == null) {
            throw new InvalidRoleException("Utilisateur non identifié pour : " + operation);
        }
        if (!allowed) {
            throw new InvalidRoleException(
                    "Le rôle " + actor.getRole() + " n'a pas la permission de " + operation + ".");
        }
    }
}
