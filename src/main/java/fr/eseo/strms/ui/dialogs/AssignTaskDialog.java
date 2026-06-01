package fr.eseo.strms.ui.dialogs;

import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Engineer;
import fr.eseo.strms.model.User;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Boîte de dialogue pour choisir l'ingénieur à qui assigner une tâche.
 * Retourne l'identifiant de l'ingénieur choisi.
 */
public class AssignTaskDialog extends Dialog<String> {

    public AssignTaskDialog(TaskManager taskManager) {
        setTitle("Assigner la tâche");
        setHeaderText("Choisir un ingénieur");

        ButtonType ok = new ButtonType("Assigner", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        // On filtre uniquement les Engineer
        List<Engineer> engineers = new ArrayList<>();
        for (User u : taskManager.getAllUsers()) {
            if (u instanceof Engineer e) engineers.add(e);
        }

        ComboBox<Engineer> combo = new ComboBox<>();
        combo.getItems().addAll(engineers);
        if (!engineers.isEmpty()) combo.setValue(engineers.get(0));

        VBox box = new VBox(10, new Label("Ingénieur :"), combo);
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
