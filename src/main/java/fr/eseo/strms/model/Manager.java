package fr.eseo.strms.model;

/**
 * Manager : assigne les tâches aux ingénieurs et suit l'avancement.
 *
 * Peut :
 * - assigner des tâches
 * - générer des rapports
 *
 * Ne peut pas créer/supprimer des tâches (réservé à l'Admin),
 * ni les exécuter (réservé à l'Engineer).
 */
public class Manager extends User {

    public Manager(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "Manager";
    }

    @Override
    public boolean canCreateTask() {
        return false;
    }

    @Override
    public boolean canDeleteTask() {
        return false;
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
