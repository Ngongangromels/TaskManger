package fr.eseo.strms.model;

import fr.eseo.strms.enums.PriorityLevel;
import fr.eseo.strms.enums.TaskCategory;
import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.exceptions.InvalidTaskStateException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Représente une tâche du système STRMS.
 *
 * Une tâche encapsule :
 * - Ses données propres (id, titre, description, priorité, statut, catégorie, deadline...)
 * - Une liste de prérequis (dépendances) : d'autres tâches qui doivent être DONE
 *   avant que celle-ci puisse passer en IN_PROGRESS.
 * - Un historique de toutes les actions effectuées (List&lt;TaskHistoryEntry&gt;).
 * - L'ingénieur assigné, ou null si la tâche n'est pas encore attribuée.
 *
 * Implémente Comparable&lt;Task&gt; pour le tri en PriorityQueue (priorité décroissante).
 *
 * Note : Cette classe applique les règles métier locales (transitions valides, ajout
 * dans l'historique). Les règles globales (cycles, permissions, etc.) sont du ressort
 * de la classe TaskManager.
 */
public class Task implements Comparable<Task> {

    private final String id;
    private String title;
    private String description;
    private PriorityLevel priority;
    private TaskStatus status;
    private TaskCategory category;
    private LocalDate deadline;
    private Engineer assignedEngineer;

    /** Liste des tâches prérequises (dépendances). */
    private final List<Task> dependencies;

    /** Historique de toutes les actions effectuées sur la tâche. */
    private final List<TaskHistoryEntry> history;

    public Task(String id, String title, String description,
                PriorityLevel priority, TaskCategory category, LocalDate deadline) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'identifiant de tâche ne peut pas être vide.");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : PriorityLevel.MEDIUM;
        this.category = category != null ? category : TaskCategory.FEATURE;
        this.deadline = deadline;
        this.status = TaskStatus.TODO;
        this.dependencies = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    // ===================== Getters / Setters =====================

    public String getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PriorityLevel getPriority() { return priority; }

    public TaskStatus getStatus() { return status; }

    public TaskCategory getCategory() { return category; }
    public void setCategory(TaskCategory category) { this.category = category; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public Engineer getAssignedEngineer() { return assignedEngineer; }
    public void setAssignedEngineer(Engineer engineer) { this.assignedEngineer = engineer; }

    /** Retourne une vue non modifiable de la liste des dépendances. */
    public List<Task> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    /** Retourne une vue non modifiable de l'historique. */
    public List<TaskHistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    // ===================== Méthodes métier locales =====================

    /**
     * Met à jour le statut en vérifiant la validité de la transition.
     *
     * Transitions autorisées :
     *   TODO        -> BLOCKED, IN_PROGRESS
     *   BLOCKED     -> TODO, IN_PROGRESS
     *   IN_PROGRESS -> DONE, BLOCKED  (ré-blocage si dépendance ajoutée)
     *   DONE        -> (terminal, aucune transition)
     */
    public void updateStatus(TaskStatus newStatus) throws InvalidTaskStateException {
        if (newStatus == null) {
            throw new InvalidTaskStateException("Le nouveau statut ne peut pas être null.");
        }
        if (this.status == TaskStatus.DONE) {
            throw new InvalidTaskStateException(
                    "La tâche " + id + " est déjà terminée (DONE) : transition impossible.");
        }
        this.status = newStatus;
    }

    /** Modifie la priorité de la tâche. */
    public void changePriority(PriorityLevel newPriority) {
        if (newPriority != null) {
            this.priority = newPriority;
        }
    }

    /** Met à jour la description. */
    public void updateDescription(String description) {
        this.description = description;
    }

    /** Marque la tâche comme terminée (DONE). */
    public void markAsDone() throws InvalidTaskStateException {
        updateStatus(TaskStatus.DONE);
    }

    /**
     * Force le statut de la tâche sans validation. À n'utiliser QUE pour la
     * restauration depuis fichier (FileManager). Pour les changements normaux,
     * utiliser updateStatus() ou les méthodes du TaskManager.
     */
    public void forceStatus(TaskStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    /**
     * Ajoute une entrée d'historique. Méthode package-private/public utilisée
     * par TaskManager principalement, mais aussi en interne par la tâche.
     */
    public void addHistoryEntry(TaskHistoryEntry entry) {
        if (entry != null) {
            this.history.add(entry);
        }
    }

    /**
     * Ajoute une dépendance à la tâche. La validation (cycles, existence) est
     * effectuée par le TaskManager avant cet appel.
     */
    public void addDependency(Task other) {
        if (other != null && !this.dependencies.contains(other) && !other.equals(this)) {
            this.dependencies.add(other);
        }
    }

    /** Retire une dépendance. */
    public boolean removeDependency(Task other) {
        return this.dependencies.remove(other);
    }

    /**
     * Indique si toutes les dépendances sont au statut DONE.
     * Une tâche sans dépendances est considérée prête.
     */
    public boolean areDependenciesCompleted() {
        for (Task dep : dependencies) {
            if (dep.getStatus() != TaskStatus.DONE) {
                return false;
            }
        }
        return true;
    }

    /** Affichage console simple. */
    public String displayTask() {
        return "Tâche [" + id + "] " + title
                + " | priorité=" + priority.getLabel()
                + " | statut=" + status.getLabel()
                + " | catégorie=" + category.getLabel()
                + " | assignée à=" + (assignedEngineer != null ? assignedEngineer.getName() : "aucun");
    }

    // ===================== Comparable / equals / hashCode =====================

    /**
     * Comparaison pour la PriorityQueue : tâche de plus haute priorité d'abord.
     * On compare par poids décroissant. À priorité égale, on tri par id pour la stabilité.
     */
    @Override
    public int compareTo(Task other) {
        int cmp = Integer.compare(other.priority.getWeight(), this.priority.getWeight());
        if (cmp == 0) {
            return this.id.compareTo(other.id);
        }
        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return displayTask();
    }
}
