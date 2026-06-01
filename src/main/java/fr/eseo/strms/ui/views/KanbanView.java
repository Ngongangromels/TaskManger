package fr.eseo.strms.ui.views;

import fr.eseo.strms.enums.NotificationType;
import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.exceptions.*;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Engineer;
import fr.eseo.strms.model.Task;
import fr.eseo.strms.model.User;
import fr.eseo.strms.ui.MainApp;
import fr.eseo.strms.ui.MainView;
import fr.eseo.strms.ui.dialogs.AddDependencyDialog;
import fr.eseo.strms.ui.dialogs.AssignTaskDialog;
import fr.eseo.strms.ui.dialogs.CreateTaskDialog;
import fr.eseo.strms.utils.NotificationManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;

/**
 * Vue Kanban : affiche les tâches en 4 colonnes correspondant aux 4 statuts
 * (TODO, BLOCKED, IN_PROGRESS, DONE) à la manière de Jira.
 *
 * Chaque carte de tâche permet, via un menu contextuel, de :
 *  - Démarrer / Terminer la tâche (Engineer assigné uniquement)
 *  - Assigner la tâche à un ingénieur (Admin/Manager)
 *  - Ajouter une dépendance
 *  - Supprimer la tâche (Admin)
 */
public class KanbanView {

    private final TaskManager taskManager;
    private final NotificationManager notificationManager;
    private final MainApp app;
    private final MainView mainView;

    private final BorderPane root;
    private final HBox columnsContainer;

    public KanbanView(TaskManager taskManager, NotificationManager notificationManager,
                      MainApp app, MainView mainView) {
        this.taskManager = taskManager;
        this.notificationManager = notificationManager;
        this.app = app;
        this.mainView = mainView;

        this.root = new BorderPane();
        this.root.setPadding(new Insets(24));

        // Barre d'actions du haut
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Tableau Kanban");
        title.getStyleClass().add("view-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNew = new Button("+ Nouvelle tâche");
        btnNew.getStyleClass().add("primary-button");
        btnNew.setOnAction(e -> onCreateTask());

        Button btnRefresh = new Button("Rafraîchir");
        btnRefresh.getStyleClass().add("secondary-button");
        btnRefresh.setOnAction(e -> refresh());

        actionBar.getChildren().addAll(title, spacer, btnRefresh, btnNew);

        this.columnsContainer = new HBox(16);
        this.columnsContainer.setPadding(new Insets(20, 0, 0, 0));

        VBox topPart = new VBox(actionBar);
        topPart.setPadding(new Insets(0, 0, 16, 0));

        ScrollPane scroll = new ScrollPane(columnsContainer);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("scroll-pane");

        root.setTop(topPart);
        root.setCenter(scroll);
    }

    public BorderPane getRoot() {
        return root;
    }

    public void refresh() {
        columnsContainer.getChildren().clear();
        columnsContainer.getChildren().addAll(
                buildColumn("À faire",       TaskStatus.TODO,        "col-todo"),
                buildColumn("Bloquées",      TaskStatus.BLOCKED,     "col-blocked"),
                buildColumn("En cours",      TaskStatus.IN_PROGRESS, "col-progress"),
                buildColumn("Terminées",    TaskStatus.DONE,        "col-done")
        );
    }

    private VBox buildColumn(String title, TaskStatus status, String cssClass) {
        VBox column = new VBox(10);
        column.getStyleClass().addAll("kanban-column", cssClass);
        column.setPadding(new Insets(12));
        column.setPrefWidth(280);

        long count = taskManager.getAllTasks().stream()
                .filter(t -> t.getStatus() == status).count();

        Label header = new Label(title + "  (" + count + ")");
        header.getStyleClass().add("kanban-header");
        column.getChildren().add(header);

        for (Task t : taskManager.getAllTasks()) {
            if (t.getStatus() == status) {
                column.getChildren().add(buildCard(t));
            }
        }
        return column;
    }

    private VBox buildCard(Task task) {
        VBox card = new VBox(6);
        card.getStyleClass().add("task-card");
        card.setPadding(new Insets(12));

        Label idLbl = new Label(task.getId());
        idLbl.getStyleClass().add("task-id");

        Label titleLbl = new Label(task.getTitle());
        titleLbl.getStyleClass().add("task-title");
        titleLbl.setWrapText(true);

        Label descLbl = new Label(
                task.getDescription() != null && task.getDescription().length() > 80
                        ? task.getDescription().substring(0, 80) + "..."
                        : task.getDescription());
        descLbl.getStyleClass().add("task-desc");
        descLbl.setWrapText(true);

        HBox meta = new HBox(6);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label priorityLbl = new Label(task.getPriority().getLabel());
        priorityLbl.getStyleClass().addAll("badge",
                "priority-" + task.getPriority().name().toLowerCase());
        Label catLbl = new Label(task.getCategory().getLabel());
        catLbl.getStyleClass().addAll("badge", "category");
        meta.getChildren().addAll(priorityLbl, catLbl);

        Label assignedLbl = new Label(
                task.getAssignedEngineer() != null
                        ? "👤 " + task.getAssignedEngineer().getName()
                        : "Non assignée");
        assignedLbl.getStyleClass().add("task-assignee");

        Label depsLbl = null;
        if (!task.getDependencies().isEmpty()) {
            StringBuilder sb = new StringBuilder("Dépend de : ");
            for (int i = 0; i < task.getDependencies().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(task.getDependencies().get(i).getId());
            }
            depsLbl = new Label(sb.toString());
            depsLbl.getStyleClass().add("task-deps");
            depsLbl.setWrapText(true);
        }

        // Boutons d'action contextuels
        HBox actions = new HBox(6);
        User actor = app.getCurrentUser();

        // Démarrage d'une tâche : Engineer assigné uniquement.
        if (task.getStatus() == TaskStatus.TODO
                && actor instanceof Engineer
                && actor.equals(task.getAssignedEngineer())) {
            Button start = new Button("Démarrer");
            start.getStyleClass().add("card-button");
            start.setOnAction(e -> onStart(task));
            actions.getChildren().add(start);
        }
        // Complétion d'une tâche.
        // - Admin / Manager  : le bouton "Terminer" fonctionne et clôture la tâche.
        // - Engineer assigné : le bouton est visible mais grisé. Au clic, une
        //   alerte explique que seuls Admin et Manager peuvent clôturer une tâche.
        if (task.getStatus() == TaskStatus.IN_PROGRESS && actor != null) {
            if (actor.canCompleteTask()) {
                Button complete = new Button("Terminer");
                complete.getStyleClass().add("card-button");
                complete.setOnAction(e -> onComplete(task));
                actions.getChildren().add(complete);
            } else if (actor instanceof Engineer
                    && actor.equals(task.getAssignedEngineer())) {
                Button complete = new Button("Terminer");
                complete.getStyleClass().addAll("card-button", "disabled");
                complete.setOnAction(e -> showEngineerCompletionDenied());
                actions.getChildren().add(complete);
            }
        }
        if (actor != null && actor.canAssignTask() && task.getStatus() != TaskStatus.DONE) {
            Button assign = new Button("Assigner");
            assign.getStyleClass().add("card-button");
            assign.setOnAction(e -> onAssign(task));
            actions.getChildren().add(assign);
        }
        if (actor != null && actor.canCreateTask()) {
            Button dep = new Button("+ Dép.");
            dep.getStyleClass().add("card-button");
            dep.setOnAction(e -> onAddDependency(task));
            actions.getChildren().add(dep);
        }
        if (actor != null && actor.canDeleteTask()) {
            Button delete = new Button("Supprimer");
            delete.getStyleClass().addAll("card-button", "danger");
            delete.setOnAction(e -> onDelete(task));
            actions.getChildren().add(delete);
        }

        card.getChildren().addAll(idLbl, titleLbl, descLbl, meta, assignedLbl);
        if (depsLbl != null) card.getChildren().add(depsLbl);
        if (!actions.getChildren().isEmpty()) card.getChildren().add(actions);

        return card;
    }

    // ==================== Actions ====================

    private void onCreateTask() {
        try {
            CreateTaskDialog dialog = new CreateTaskDialog();
            dialog.showAndWait().ifPresent(task -> {
                try {
                    taskManager.addTask(task, app.getCurrentUser());
                    notificationManager.notify(app.getCurrentUser(),
                            "Tâche " + task.getId() + " créée.", NotificationType.CONSOLE);
                    mainView.refreshAll();
                } catch (InvalidRoleException | DuplicateTaskException ex) {
                    showError(ex.getMessage());
                }
            });
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onStart(Task task) {
        try {
            taskManager.startTask(task.getId(), app.getCurrentUser());
            notificationManager.notify(app.getCurrentUser(),
                    "Tâche " + task.getId() + " démarrée.", NotificationType.CONSOLE);
            mainView.refreshAll();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onComplete(Task task) {
        try {
            taskManager.completeTask(task.getId(), app.getCurrentUser());
            notificationManager.notify(app.getCurrentUser(),
                    "Tâche " + task.getId() + " terminée.", NotificationType.CONSOLE);
            mainView.refreshAll();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void onAssign(Task task) {
        AssignTaskDialog dialog = new AssignTaskDialog(taskManager);
        dialog.showAndWait().ifPresent(engineerId -> {
            try {
                taskManager.assignTask(task.getId(), engineerId, app.getCurrentUser());
                notificationManager.notify(taskManager.getUser(engineerId),
                        "Tâche " + task.getId() + " vous a été assignée.",
                        NotificationType.CONSOLE);
                mainView.refreshAll();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void onAddDependency(Task task) {
        AddDependencyDialog dialog = new AddDependencyDialog(taskManager, task.getId());
        dialog.showAndWait().ifPresent(dependsOnId -> {
            try {
                taskManager.addDependency(task.getId(), dependsOnId, app.getCurrentUser());
                mainView.refreshAll();
            } catch (CircularDependencyException ex) {
                showError("Cycle détecté : " + ex.getMessage());
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void onDelete(Task task) {
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Supprimer la tâche " + task.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation de suppression");
        confirm.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    taskManager.deleteTask(task.getId(), app.getCurrentUser());
                    mainView.refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.showAndWait();
    }

    /**
     * Alerte affichée quand un Engineer tente de terminer une tâche.
     * Règle métier : seuls Admin et Manager peuvent clôturer une tâche.
     */
    private void showEngineerCompletionDenied() {
        Alert alert = new Alert(AlertType.WARNING,
                "En tant qu'ingénieur, vous ne pouvez pas terminer une tâche.\n\n"
                        + "Seuls les rôles Admin et Manager sont autorisés à "
                        + "marquer une tâche comme terminée.\n\n"
                        + "Demandez à votre Manager de clôturer la tâche pour vous.",
                ButtonType.OK);
        alert.setHeaderText("Action non autorisée");
        alert.setTitle("Permission refusée");
        alert.showAndWait();
    }
}
