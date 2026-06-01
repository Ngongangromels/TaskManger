package fr.eseo.strms.utils;

import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Génère un rapport synthétique sur l'ensemble des tâches du système.
 * Implémente l'interface Reportable (polymorphisme).
 */
public class ReportGenerator implements Reportable {

    private final TaskManager taskManager;

    public ReportGenerator(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("  RAPPORT STRMS - ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
          .append("\n");
        sb.append("=========================================\n");

        int total = taskManager.getAllTasks().size();
        sb.append("Nombre total de tâches : ").append(total).append("\n");

        // Compteurs par statut
        int todo = 0, blocked = 0, inProgress = 0, done = 0;
        for (Task t : taskManager.getAllTasks()) {
            switch (t.getStatus()) {
                case TODO -> todo++;
                case BLOCKED -> blocked++;
                case IN_PROGRESS -> inProgress++;
                case DONE -> done++;
            }
        }
        sb.append("  - À faire (TODO)    : ").append(todo).append("\n");
        sb.append("  - Bloquées          : ").append(blocked).append("\n");
        sb.append("  - En cours          : ").append(inProgress).append("\n");
        sb.append("  - Terminées (DONE)  : ").append(done).append("\n");
        sb.append("-----------------------------------------\n");

        // Détails de chaque tâche
        sb.append("Détail :\n");
        for (Task t : taskManager.getAllTasks()) {
            sb.append("  • ").append(t.displayTask()).append("\n");
        }
        sb.append("=========================================\n");
        return sb.toString();
    }
}
