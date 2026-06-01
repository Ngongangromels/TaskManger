package fr.eseo.strms.enums;

/**
 * Énumération représentant l'état (statut) d'une tâche.
 *
 * Cycle de vie d'une tâche :
 *   TODO -> BLOCKED -> IN_PROGRESS -> DONE
 *
 * Règles :
 * - TODO        : la tâche est créée mais le travail n'a pas encore commencé.
 * - BLOCKED     : la tâche ne peut pas avancer car des dépendances ne sont pas terminées.
 * - IN_PROGRESS : la tâche est en cours d'exécution par un ingénieur.
 * - DONE        : la tâche est terminée (état terminal, irréversible).
 */
public enum TaskStatus {
    TODO("À faire"),
    BLOCKED("Bloquée"),
    IN_PROGRESS("En cours"),
    DONE("Terminée");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
