package fr.eseo.strms.model;

import java.util.Objects;

/**
 * Classe abstraite représentant un utilisateur du système STRMS.
 *
 * Cette classe applique les principes de la POO :
 * - Encapsulation : les attributs sont privés, accessibles via getters/setters.
 * - Héritage      : Admin, Manager et Engineer héritent de User.
 * - Polymorphisme : les méthodes canCreateTask(), canDeleteTask(), etc.
 *                   sont redéfinies dans chaque sous-classe.
 * - Abstraction   : on ne peut pas instancier User directement.
 *
 * Chaque sous-classe doit indiquer ses droits via les méthodes abstraites.
 */
public abstract class User {

    private final String id;
    private String name;
    private String email;

    /**
     * Constructeur protégé : seules les sous-classes peuvent créer un utilisateur.
     */
    protected User(String id, String name, String email) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'identifiant utilisateur ne peut pas être vide.");
        }
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retourne le libellé du rôle (ex : "Admin", "Manager", "Engineer").
     * Utilisé pour l'affichage et les rapports.
     */
    public abstract String getRole();

    // ====== Permissions (polymorphisme) ======

    /** Peut créer une nouvelle tâche dans le système. */
    public abstract boolean canCreateTask();

    /** Peut supprimer une tâche existante. */
    public abstract boolean canDeleteTask();

    /** Peut assigner une tâche à un ingénieur. */
    public abstract boolean canAssignTask();

    /** Peut exécuter (démarrer une tâche, la passer en IN_PROGRESS). */
    public abstract boolean canExecuteTask();

    /**
     * Peut marquer une tâche comme terminée (DONE).
     *
     * Règle métier : seuls l'Admin et le Manager peuvent valider la
     * complétion d'une tâche. L'Engineer peut la démarrer mais pas la
     * clôturer (pour garder un contrôle hiérarchique).
     */
    public abstract boolean canCompleteTask();

    /** Peut générer des rapports. */
    public abstract boolean canGenerateReport();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getRole() + "{id='" + id + "', name='" + name + "'}";
    }
}
