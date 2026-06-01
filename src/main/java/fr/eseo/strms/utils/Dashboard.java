package fr.eseo.strms.utils;

import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Engineer;
import fr.eseo.strms.model.Task;
import fr.eseo.strms.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard : affiche des statistiques agrégées sur l'état du système.
 *
 * Les statistiques fournies :
 *  - Nombre de tâches par statut
 *  - Nombre de tâches assignées à chaque ingénieur
 *  - Tâches en retard (deadline dépassée)
 */
public class Dashboard {

    private final TaskManager taskManager;

    public Dashboard(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /** Compte le nombre de tâches par statut. */
    public Map<TaskStatus, Integer> countTasksByStatus() {
        Map<TaskStatus, Integer> result = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            result.put(status, 0);
        }
        for (Task t : taskManager.getAllTasks()) {
            result.merge(t.getStatus(), 1, Integer::sum);
        }
        return result;
    }

    /** Compte le nombre de tâches assignées par utilisateur (par nom). */
    public Map<String, Integer> countTasksByUser() {
        Map<String, Integer> result = new HashMap<>();
        // Initialise tous les ingénieurs avec 0
        for (User u : taskManager.getAllUsers()) {
            if (u instanceof Engineer) {
                result.put(u.getName(), 0);
            }
        }
        for (Task t : taskManager.getAllTasks()) {
            if (t.getAssignedEngineer() != null) {
                result.merge(t.getAssignedEngineer().getName(), 1, Integer::sum);
            }
        }
        return result;
    }

    /** Retourne le nombre de tâches en retard (deadline dépassée et statut != DONE). */
    public int countOverdueTasks() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Task t : taskManager.getAllTasks()) {
            if (t.getDeadline() != null
                    && t.getDeadline().isBefore(today)
                    && t.getStatus() != TaskStatus.DONE) {
                count++;
            }
        }
        return count;
    }

    /** Affichage console synthétique. */
    public void printDashboard() {
        System.out.println("=== TABLEAU DE BORD STRMS ===");
        System.out.println("Tâches par statut :");
        countTasksByStatus().forEach((status, count) ->
                System.out.println("  - " + status.getLabel() + " : " + count));
        System.out.println("Tâches assignées par ingénieur :");
        countTasksByUser().forEach((name, count) ->
                System.out.println("  - " + name + " : " + count));
        System.out.println("Tâches en retard : " + countOverdueTasks());
    }
}
