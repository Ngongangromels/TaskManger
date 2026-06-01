package fr.eseo.strms.ui.dialogs;

import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Task;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Boîte de dialogue pour ajouter une dépendance à une tâche.
 *
 * On affiche un combobox avec toutes les autres tâches existantes (sauf la tâche cible).
 * Retourne l'identifiant de la tâche dont on dépend.
 */
public class AddDependencyDialog extends Dialog<String> {

    public AddDependencyDialog(TaskManager taskManager, String currentTaskId) {
        setTitle("Ajouter une dépendance");
        setHeaderText("La tâche " + currentTaskId + " va dépendre de :");

        ButtonType ok = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        // On exclut la tâche elle-même
        List<Task> candidates = new ArrayList<>();
        for (Task t : taskManager.getAllTasks()) {
            if (!t.getId().equals(currentTaskId)) {
                candidates.add(t);
            }
        }

        ComboBox<Task> combo = new ComboBox<>();
        combo.getItems().addAll(candidates);
        if (!candidates.isEmpty()) combo.setValue(candidates.get(0));

        VBox box = new VBox(10, new Label("Tâche prérequise :"), combo);
        box.setPadding(new Insets(20));
        getDialogPane().setContent(box);

        Node btn = getDialogPane().lookupButton(ok);
        btn.setDisable(combo.getValue() == null);
        combo.valueProperty().addListener((obs, o, n) -> btn.setDisable(n == null));

        setResultConverter(b -> (b == ok && combo.getValue() != null)
                ? combo.getValue().getId()
                : null);
    }
}
