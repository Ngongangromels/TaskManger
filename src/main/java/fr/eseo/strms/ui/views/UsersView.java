package fr.eseo.strms.ui.views;

import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * Vue affichant la liste des utilisateurs et leurs droits.
 * Présente, pour chaque utilisateur, son rôle et un récapitulatif des permissions.
 */
public class UsersView {

    private final TaskManager taskManager;
    private final VBox root;
    private final TableView<UserRow> table;

    public UsersView(TaskManager taskManager) {
        this.taskManager = taskManager;

        this.root = new VBox(16);
        this.root.setPadding(new Insets(24));

        Label title = new Label("Utilisateurs");
        title.getStyleClass().add("view-title");

        Label subtitle = new Label("Liste des utilisateurs enregistrés et de leurs droits");
        subtitle.getStyleClass().add("view-subtitle");

        this.table = buildTable();

        root.getChildren().addAll(title, subtitle, table);
    }

    public VBox getRoot() {
        return root;
    }

    @SuppressWarnings("deprecation")
    private TableView<UserRow> buildTable() {
        TableView<UserRow> tv = new TableView<>();
        // Politique simple pour répartir les colonnes proportionnellement
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getStyleClass().add("users-table");

        TableColumn<UserRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<UserRow, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<UserRow, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<UserRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<UserRow, String> rightsCol = new TableColumn<>("Permissions");
        rightsCol.setCellValueFactory(new PropertyValueFactory<>("rights"));

        tv.getColumns().add(idCol);
        tv.getColumns().add(nameCol);
        tv.getColumns().add(roleCol);
        tv.getColumns().add(emailCol);
        tv.getColumns().add(rightsCol);
        return tv;
    }

    public void refresh() {
        ObservableList<UserRow> rows = FXCollections.observableArrayList();
        for (User u : taskManager.getAllUsers()) {
            rows.add(new UserRow(u));
        }
        table.setItems(rows);
    }

    /**
     * Classe interne (DTO) pour adapter un User aux colonnes de la TableView.
     * Doit être public pour PropertyValueFactory.
     */
    public static class UserRow {
        private final String id;
        private final String name;
        private final String role;
        private final String email;
        private final String rights;

        public UserRow(User u) {
            this.id = u.getId();
            this.name = u.getName();
            this.role = u.getRole();
            this.email = u.getEmail();
            StringBuilder r = new StringBuilder();
            if (u.canCreateTask())     r.append("Créer ");
            if (u.canDeleteTask())     r.append("Supprimer ");
            if (u.canAssignTask())     r.append("Assigner ");
            if (u.canExecuteTask())    r.append("Démarrer ");
            if (u.canCompleteTask())   r.append("Terminer ");
            if (u.canGenerateReport()) r.append("Rapports ");
            this.rights = r.toString().trim();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getEmail() { return email; }
        public String getRights() { return rights; }
    }
}
