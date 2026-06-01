package fr.eseo.strms.ui.dialogs;

import fr.eseo.strms.enums.PriorityLevel;
import fr.eseo.strms.enums.TaskCategory;
import fr.eseo.strms.model.Task;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

/**
 * Boîte de dialogue de création d'une nouvelle tâche.
 *
 * Demande à l'utilisateur :
 *   - id (unique)
 *   - titre
 *   - description
 *   - priorité (combobox)
 *   - catégorie (combobox)
 *   - deadline (DatePicker)
 *
 * Retourne une Task prête à être ajoutée au TaskManager.
 */
public class CreateTaskDialog extends Dialog<Task> {

    public CreateTaskDialog() {
        setTitle("Nouvelle tâche");
        setHeaderText("Renseignez les informations de la tâche");

        ButtonType validate = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(validate, ButtonType.CANCEL);

        TextField idField = new TextField();
        idField.setPromptText("Ex : T-006");

        TextField titleField = new TextField();
        titleField.setPromptText("Titre de la tâche");

        TextArea descField = new TextArea();
        descField.setPromptText("Description...");
        descField.setPrefRowCount(3);
        descField.setWrapText(true);

        ComboBox<PriorityLevel> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(PriorityLevel.values());
        priorityBox.setValue(PriorityLevel.MEDIUM);

        ComboBox<TaskCategory> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(TaskCategory.values());
        categoryBox.setValue(TaskCategory.FEATURE);

        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusDays(7));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        grid.add(new Label("Identifiant :"), 0, 0);   grid.add(idField, 1, 0);
        grid.add(new Label("Titre :"), 0, 1);          grid.add(titleField, 1, 1);
        grid.add(new Label("Description :"), 0, 2);    grid.add(descField, 1, 2);
        grid.add(new Label("Priorité :"), 0, 3);       grid.add(priorityBox, 1, 3);
        grid.add(new Label("Catégorie :"), 0, 4);      grid.add(categoryBox, 1, 4);
        grid.add(new Label("Deadline :"), 0, 5);       grid.add(deadlinePicker, 1, 5);

        getDialogPane().setContent(grid);

        // Validation : empêche le bouton Créer si id ou titre vide
        Node createBtn = getDialogPane().lookupButton(validate);
        createBtn.setDisable(true);
        Runnable updateState = () -> createBtn.setDisable(
                idField.getText().isBlank() || titleField.getText().isBlank());
        idField.textProperty().addListener((obs, o, n) -> updateState.run());
        titleField.textProperty().addListener((obs, o, n) -> updateState.run());

        setResultConverter(buttonType -> {
            if (buttonType == validate) {
                return new Task(
                        idField.getText().trim(),
                        titleField.getText().trim(),
                        descField.getText(),
                        priorityBox.getValue(),
                        categoryBox.getValue(),
                        deadlinePicker.getValue());
            }
            return null;
        });
    }
}
