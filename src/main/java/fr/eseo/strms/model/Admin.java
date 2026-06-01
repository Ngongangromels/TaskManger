package fr.eseo.strms.model;

/**
 * Administrateur : possède tous les droits dans le système.
 *
 * Peut :
 * - créer, supprimer, assigner des tâches
 * - générer des rapports
 *
 * Ne peut PAS exécuter (travailler sur) une tâche : seul l'Engineer peut le faire.
 * (D'après le cahier des charges : "Only engineers can execute tasks".)
 */
public class Admin extends User {

    public Admin(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public boolean canCreateTask() {
        return true;
    }

    @Override
    public boolean canDeleteTask() {
        return true;
    }

    @Override
    public boolean canAssignTask() {
        return true;
    }

    @Override
    public boolean canExecuteTask() {
        return false;
    }

    @Override
    public boolean canCompleteTask() {
        return true;
    }

    @Override
    public boolean canGenerateReport() {
        return true;
    }
}
