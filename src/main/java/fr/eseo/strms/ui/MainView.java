package fr.eseo.strms.ui;

import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.User;
import fr.eseo.strms.ui.views.*;
import fr.eseo.strms.utils.NotificationManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Vue principale de l'application : compose la sidebar (navigation),
 * la barre supérieure (utilisateur connecté) et la zone de contenu central.
 *
 * Layout :
 *
 *   +-----------------------------------------------------+
 *   |   Top Bar (titre, sélection utilisateur)            |
 *   +----------+------------------------------------------+
 *   | Sidebar  |  Zone de contenu (Dashboard, Kanban...)  |
 *   |          |                                          |
 *   |          |                                          |
 *   +----------+------------------------------------------+
 */
public class MainView {

    private final BorderPane root;
    private final TaskManager taskManager;
    private final NotificationManager notificationManager;
    private final MainApp app;

    private final StackPane contentArea;
    private Label statusLabel;

    // Vues
    private DashboardView dashboardView;
    private KanbanView kanbanView;
    private UsersView usersView;
    private HistoryView historyView;
    private ReportView reportView;

    public MainView(TaskManager taskManager, NotificationManager notificationManager, MainApp app) {
        this.taskManager = taskManager;
        this.notificationManager = notificationManager;
        this.app = app;

        this.root = new BorderPane();
        this.root.getStyleClass().add("main-root");

        this.contentArea = new StackPane();
        this.contentArea.getStyleClass().add("content-area");

        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());
        root.setCenter(contentArea);

        // Vue initiale : tableau de bord
        showDashboard();
    }

    public Parent getRoot() {
        return root;
    }

    /** Barre supérieure : titre + sélection utilisateur courant. */
    private HBox buildTopBar() {
        HBox topBar = new HBox(20);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(12, 24, 12, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label appTitle = new Label("STRMS");
        appTitle.getStyleClass().add("app-title");

        Label appSubtitle = new Label("Smart Task & Resource Management System");
        appSubtitle.getStyleClass().add("app-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Utilisateur :");
        userLabel.getStyleClass().add("top-label");

        ComboBox<User> userSelector = new ComboBox<>();
        userSelector.getItems().addAll(taskManager.getAllUsers());
        userSelector.setValue(app.getCurrentUser());
        userSelector.getStyleClass().add("user-selector");
        userSelector.setOnAction(e -> {
            User selected = userSelector.getValue();
            if (selected != null) {
                app.setCurrentUser(selected);
                refreshAll();
                setStatus("Utilisateur courant : " + selected.getName()
                        + " (" + selected.getRole() + ")");
            }
        });

        topBar.getChildren().addAll(appTitle, appSubtitle, spacer, userLabel, userSelector);
        return topBar;
    }

    /** Sidebar : boutons de navigation entre les vues. */
    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.setPrefWidth(220);

        Label section = new Label("NAVIGATION");
        section.getStyleClass().add("sidebar-section");

        Button btnDashboard = navButton("Tableau de bord", e -> showDashboard());
        Button btnKanban    = navButton("Tâches (Kanban)", e -> showKanban());
        Button btnUsers     = navButton("Utilisateurs",     e -> showUsers());
        Button btnHistory   = navButton("Historique",       e -> showHistory());
        Button btnReport    = navButton("Rapports",         e -> showReport());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);

        sidebar.getChildren().addAll(section,
                btnDashboard, btnKanban, btnUsers, btnHistory, btnReport,
                spacer, statusLabel);
        return sidebar;
    }

    private Button navButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(action);
        return b;
    }

    // ==================== Navigation ====================

    private void showDashboard() {
        if (dashboardView == null) {
            dashboardView = new DashboardView(taskManager);
        }
        dashboardView.refresh();
        setContent(dashboardView.getRoot());
        setStatus("Vue : Tableau de bord");
    }

    private void showKanban() {
        if (kanbanView == null) {
            kanbanView = new KanbanView(taskManager, notificationManager, app, this);
        }
        kanbanView.refresh();
        setContent(kanbanView.getRoot());
        setStatus("Vue : Tâches (Kanban)");
    }

    private void showUsers() {
        if (usersView == null) {
            usersView = new UsersView(taskManager);
        }
        usersView.refresh();
        setContent(usersView.getRoot());
        setStatus("Vue : Utilisateurs");
    }

    private void showHistory() {
        if (historyView == null) {
            historyView = new HistoryView(taskManager);
        }
        historyView.refresh();
        setContent(historyView.getRoot());
        setStatus("Vue : Historique des tâches");
    }

    private void showReport() {
        if (reportView == null) {
            reportView = new ReportView(taskManager);
        }
        reportView.refresh();
        setContent(reportView.getRoot());
        setStatus("Vue : Rapports");
    }

    private void setContent(Parent node) {
        contentArea.getChildren().setAll(node);
    }

    public void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /** Force le rafraîchissement de toutes les vues (à appeler après modification). */
    public void refreshAll() {
        if (dashboardView != null) dashboardView.refresh();
        if (kanbanView    != null) kanbanView.refresh();
        if (usersView     != null) usersView.refresh();
        if (historyView   != null) historyView.refresh();
        if (reportView    != null) reportView.refresh();
    }
}
