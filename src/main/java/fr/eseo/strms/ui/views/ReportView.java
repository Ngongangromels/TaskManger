package fr.eseo.strms.ui.views;

import fr.eseo.strms.exceptions.FilePersistenceException;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.utils.FileManager;
import fr.eseo.strms.utils.ReportGenerator;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Vue Rapports : génère un rapport texte synthétique des tâches
 * et offre des actions de persistance (sauver, charger, exporter).
 */
public class ReportView {

    private final TaskManager taskManager;
    private final ReportGenerator reportGenerator;
    private final FileManager fileManager;

    private final VBox root;
    private final TextArea reportArea;

    public ReportView(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.reportGenerator = new ReportGenerator(taskManager);
        this.fileManager = new FileManager();

        this.root = new VBox(16);
        this.root.setPadding(new Insets(24));

        Label title = new Label("Rapports");
        title.getStyleClass().add("view-title");

        Label subtitle = new Label("Génération de rapports et persistance des données");
        subtitle.getStyleClass().add("view-subtitle");

        // On crée la zone de texte avant les boutons pour pouvoir la référencer
        // depuis les lambdas de leurs handlers.
        this.reportArea = new TextArea();
        this.reportArea.setEditable(false);
        this.reportArea.setWrapText(false);
        this.reportArea.getStyleClass().add("report-area");
        VBox.setVgrow(reportArea, Priority.ALWAYS);

        // Barre d'actions
        HBox actions = new HBox(10);
        Button btnGenerate = new Button("Générer le rapport");
        btnGenerate.getStyleClass().add("primary-button");
        btnGenerate.setOnAction(e -> reportArea.setText(reportGenerator.generateReport()));

        Button btnExport = new Button("Exporter en .txt");
        btnExport.getStyleClass().add("secondary-button");
        btnExport.setOnAction(e -> exportReport());

        Button btnSaveTasks = new Button("Sauvegarder les tâches");
        btnSaveTasks.getStyleClass().add("secondary-button");
        btnSaveTasks.setOnAction(e -> saveTasks());

        Button btnLoadTasks = new Button("Charger des tâches");
        btnLoadTasks.getStyleClass().add("secondary-button");
        btnLoadTasks.setOnAction(e -> loadTasks());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actions.getChildren().addAll(btnGenerate, btnExport, spacer, btnSaveTasks, btnLoadTasks);

        root.getChildren().addAll(title, subtitle, actions, reportArea);
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        // Régénère le rapport pour refléter l'état courant
        reportArea.setText(reportGenerator.generateReport());
    }

    private void exportReport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter le rapport");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier texte", "*.txt"));
        chooser.setInitialFileName("rapport-strms.txt");
        File f = chooser.showSaveDialog(root.getScene().getWindow());
        if (f != null) {
            try {
                fileManager.writeReport(reportArea.getText(), f.getAbsolutePath());
                showInfo("Rapport exporté : " + f.getAbsolutePath());
            } catch (FilePersistenceException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void saveTasks() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sauvegarder les tâches");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Données STRMS", "*.strms"));
        chooser.setInitialFileName("tasks.strms");
        File f = chooser.showSaveDialog(root.getScene().getWindow());
        if (f != null) {
            try {
                fileManager.saveTasksToFile(taskManager, f.getAbsolutePath());
                showInfo("Tâches sauvegardées : " + f.getAbsolutePath());
            } catch (FilePersistenceException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void loadTasks() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Charger les tâches");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Données STRMS", "*.strms"));
        File f = chooser.showOpenDialog(root.getScene().getWindow());
        if (f != null) {
            try {
                fileManager.loadTasksFromFile(taskManager, f.getAbsolutePath());
                showInfo("Tâches chargées : " + f.getAbsolutePath());
                refresh();
            } catch (FilePersistenceException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Information");
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Erreur");
        a.showAndWait();
    }
}
