package fr.eseo.strms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Représente une entrée d'historique pour une tâche.
 *
 * Chaque entrée enregistre :
 * - L'action effectuée (création, changement de statut, assignation, etc.)
 * - L'utilisateur ayant réalisé l'action
 * - La date et heure de l'action
 * - Une description textuelle détaillée
 *
 * Une entrée d'historique est IMMUABLE : ses champs sont final et il n'y a aucun setter.
 * Cela garantit la fiabilité de l'audit (on ne peut pas réécrire l'historique).
 */
public final class TaskHistoryEntry {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String action;
    private final String performedBy;
    private final LocalDateTime timestamp;
    private final String description;

    /**
     * Construit une entrée d'historique avec horodatage automatique (instant courant).
     */
    public TaskHistoryEntry(String action, String performedBy, String description) {
        this(action, performedBy, description, LocalDateTime.now());
    }

    /**
     * Construit une entrée d'historique avec horodatage explicite (utile pour la lecture depuis fichier).
     */
    public TaskHistoryEntry(String action, String performedBy, String description, LocalDateTime timestamp) {
        this.action = Objects.requireNonNull(action, "L'action ne peut pas être nulle");
        this.performedBy = performedBy != null ? performedBy : "système";
        this.description = description != null ? description : "";
        this.timestamp = Objects.requireNonNull(timestamp, "L'horodatage ne peut pas être nul");
    }

    public String getAction() {
        return action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Représentation textuelle utilisée à la fois pour l'affichage et la sérialisation fichier.
     */
    @Override
    public String toString() {
        return "[" + FORMATTER.format(timestamp) + "] (" + performedBy + ") "
                + action + " - " + description;
    }
}
