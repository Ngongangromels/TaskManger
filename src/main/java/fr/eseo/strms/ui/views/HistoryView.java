package fr.eseo.strms.ui.views;

import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Task;
import fr.eseo.strms.model.TaskHistoryEntry;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Vue affichant l'historique des actions effectuées sur une tâche.
 * Permet de sélectionner une tâche et de voir toutes ses entrées
 * (création, changements de statut, dépendances ajoutées, etc.).
 */
public class HistoryView {

    private final TaskManager taskManager;
    private final VBox root;
    private final ComboBox<Task> taskSelector;
    private final ListView<String> historyList;

    public HistoryView(TaskManager taskManager) {
        this.taskManager = taskManager;

        this.root = new VBox(16);
        this.root.setPadding(new Insets(24));

        Label title = new Label("Historique des tâches");
        title.getStyleClass().add("view-title");

        Label subtitle = new Label("Audit des actions effectuées sur chaque tâche");
        subtitle.getStyleClass().add("view-subtitle");

        this.taskSelector = new ComboBox<>();
        taskSelector.setPromptText("Choisir une tâche...");
        taskSelector.setOnAction(e -> showHistoryFor(taskSelector.getValue()));

        HBox selectorRow = new HBox(10, new Label("Tâche :"), taskSelector);
        selectorRow.setPadding(new Insets(0, 0, 8, 0));

        this.historyList = new ListView<>();
        this.historyList.getStyleClass().add("history-list");
        VBox.setVgrow(historyList, javafx.scene.layout.Priority.ALWAYS);

        root.getChildren().addAll(title, subtitle, selectorRow, historyList);
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        Task previous = taskSelector.getValue();
        taskSelector.getItems().setAll(taskManager.getAllTasks());
        if (previous != null && taskSelector.getItems().contains(previous)) {
            taskSelector.setValue(previous);
            showHistoryFor(previous);
        } else if (!taskSelector.getItems().isEmpty()) {
            taskSelector.setValue(taskSelector.getItems().get(0));
            showHistoryFor(taskSelector.getValue());
        } else {
            historyList.getItems().clear();
        }
    }

    private void showHistoryFor(Task t) {
        historyList.getItems().clear();
        if (t == null) return;
        for (TaskHistoryEntry entry : t.getHistory()) {
            historyList.getItems().add(entry.toString());
        }
        if (historyList.getItems().isEmpty()) {
            historyList.getItems().add("(Aucune entrée d'historique pour cette tâche)");
        }
    }
}
