package fr.eseo.strms.model;

/**
 * Engineer (Ingénieur) : seul rôle autorisé à exécuter et compléter une tâche.
 *
 * Peut :
 * - exécuter une tâche qui lui est assignée (la passer à IN_PROGRESS, puis à DONE)
 *
 * Ne peut PAS créer, supprimer, assigner des tâches, ni générer de rapports.
 */
public class Engineer extends User {

    public Engineer(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "Engineer";
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
        return false;
    }

    @Override
    public boolean canExecuteTask() {
        return true;
    }

    @Override
    public boolean canCompleteTask() {
        return false;
    }

    @Override
    public boolean canGenerateReport() {
        return false;
    }
}
