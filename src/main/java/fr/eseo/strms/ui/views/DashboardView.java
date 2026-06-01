package fr.eseo.strms.ui.views;

import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Task;
import fr.eseo.strms.utils.Dashboard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vue "Tableau de bord" : affiche des statistiques et indicateurs clés
 * (KPI) sur l'état du système.
 */
public class DashboardView {

    private final TaskManager taskManager;
    private final Dashboard dashboard;
    private final ScrollPane root;
    private final VBox content;

    public DashboardView(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.dashboard = new Dashboard(taskManager);

        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));

        this.root = new ScrollPane(content);
        this.root.setFitToWidth(true);
        this.root.getStyleClass().add("scroll-pane");
    }

    public ScrollPane getRoot() {
        return root;
    }

    public void refresh() {
        content.getChildren().clear();

        Label title = new Label("Tableau de bord");
        title.getStyleClass().add("view-title");

        Label subtitle = new Label("Vue d'ensemble du système");
        subtitle.getStyleClass().add("view-subtitle");

        // KPI Cards par statut
        HBox kpiRow = new HBox(16);
        Map<TaskStatus, Integer> statusCounts = dashboard.countTasksByStatus();
        kpiRow.getChildren().addAll(
                kpiCard("À faire",     statusCounts.get(TaskStatus.TODO),        "kpi-todo"),
                kpiCard("Bloquées",    statusCounts.get(TaskStatus.BLOCKED),     "kpi-blocked"),
                kpiCard("En cours",    statusCounts.get(TaskStatus.IN_PROGRESS), "kpi-progress"),
                kpiCard("Terminées",   statusCounts.get(TaskStatus.DONE),        "kpi-done")
        );

        // Carte récap
        VBox infoCard = new VBox(8);
        infoCard.getStyleClass().add("card");
        Label infoTitle = new Label("Indicateurs principaux");
        infoTitle.getStyleClass().add("card-title");

        int total = taskManager.getAllTasks().size();
        int overdue = dashboard.countOverdueTasks();
        int inProg = taskManager.getInProgressTasks().size();
        int ready = taskManager.getReadyQueueSize();

        infoCard.getChildren().addAll(infoTitle,
                infoLine("Nombre total de tâches",   String.valueOf(total)),
                infoLine("Tâches en cours",          String.valueOf(inProg)),
                infoLine("Tâches prêtes (priorité)", String.valueOf(ready)),
                infoLine("Tâches en retard",         String.valueOf(overdue)),
                infoLine("Nombre d'utilisateurs",    String.valueOf(taskManager.getAllUsers().size()))
        );

        // Carte tâches par utilisateur
        VBox userCard = new VBox(8);
        userCard.getStyleClass().add("card");
        Label userTitle = new Label("Tâches assignées par ingénieur");
        userTitle.getStyleClass().add("card-title");
        userCard.getChildren().add(userTitle);

        Map<String, Integer> byUser = dashboard.countTasksByUser();
        if (byUser.isEmpty()) {
            userCard.getChildren().add(new Label("Aucun ingénieur enregistré."));
        } else {
            byUser.forEach((name, count) ->
                    userCard.getChildren().add(infoLine(name, count + " tâche(s)")));
        }

        // Carte "Tâches terminées" : liste détaillée des tâches DONE.
        VBox doneCard = new VBox(8);
        doneCard.getStyleClass().add("card");
        int doneCount = statusCounts.get(TaskStatus.DONE);
        Label doneTitle = new Label("Tâches terminées (" + doneCount + ")");
        doneTitle.getStyleClass().add("card-title");
        doneCard.getChildren().add(doneTitle);

        List<Task> doneTasks = new ArrayList<>();
        for (Task t : taskManager.getAllTasks()) {
            if (t.getStatus() == TaskStatus.DONE) {
                doneTasks.add(t);
            }
        }

        if (doneTasks.isEmpty()) {
            Label empty = new Label("Aucune tâche terminée pour le moment.");
            empty.getStyleClass().add("info-key");
            doneCard.getChildren().add(empty);
        } else {
            for (Task t : doneTasks) {
                String assignee = t.getAssignedEngineer() != null
                        ? t.getAssignedEngineer().getName()
                        : "non assignée";
                doneCard.getChildren().add(
                        infoLine("✓ " + t.getId() + " — " + t.getTitle(), assignee));
            }
        }

        content.getChildren().addAll(title, subtitle, kpiRow, infoCard, userCard, doneCard);
    }

    private VBox kpiCard(String label, int value, String cssClass) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().addAll("kpi-card", cssClass);
        box.setPrefWidth(200);
        box.setPadding(new Insets(20));

        Label valueLbl = new Label(String.valueOf(value));
        valueLbl.getStyleClass().add("kpi-value");

        Label nameLbl = new Label(label);
        nameLbl.getStyleClass().add("kpi-label");

        box.getChildren().addAll(valueLbl, nameLbl);
        return box;
    }

    private HBox infoLine(String key, String value) {
        HBox h = new HBox(8);
        h.setAlignment(Pos.CENTER_LEFT);
        Label k = new Label(key + " :");
        k.getStyleClass().add("info-key");
        Label v = new Label(value);
        v.getStyleClass().add("info-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        h.getChildren().addAll(k, spacer, v);
        return h;
    }
}
