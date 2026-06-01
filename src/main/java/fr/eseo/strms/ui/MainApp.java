package fr.eseo.strms.ui;

import fr.eseo.strms.enums.PriorityLevel;
import fr.eseo.strms.enums.TaskCategory;
import fr.eseo.strms.exceptions.*;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.*;
import fr.eseo.strms.utils.NotificationManager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Point d'entrée de l'application STRMS (Smart Task & Resource Management System).
 *
 * Cette classe :
 *  - Initialise le TaskManager (état de l'application).
 *  - Crée des utilisateurs et des tâches de démonstration pour faciliter les tests.
 *  - Lance la fenêtre principale.
 */
public class MainApp extends Application {

    private TaskManager taskManager;
    private NotificationManager notificationManager;
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        taskManager = new TaskManager();
        notificationManager = new NotificationManager();

        // Initialisation des données de démonstration
        seedDemoData();

        // L'utilisateur courant par défaut est l'admin (pour avoir tous les droits initialement)
        currentUser = taskManager.getUser("U-ADMIN");

        MainView mainView = new MainView(taskManager, notificationManager, this);
        Scene scene = new Scene(mainView.getRoot(), 1300, 800);

        // Application de la feuille de style
        try {
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("CSS non chargé : " + e.getMessage());
        }

        primaryStage.setTitle("STRMS - Gestion des tâches");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Crée des utilisateurs et tâches de démonstration pour pouvoir
     * tester rapidement toutes les fonctionnalités depuis l'interface.
     */
    private void seedDemoData() {
        // Utilisateurs
        Admin romels = new Admin("U-ADMIN", "Romels (Admin)", "romels@strms.fr");
        Manager danil = new Manager("U-MGR", "Danil (Manager)", "danil@strms.fr");
        Engineer kylan = new Engineer("U-ENG-1", "Kylan (Engineer)", "kylan@strms.fr");
        Engineer nathan = new Engineer("U-ENG-2", "Nathan (Engineer)", "nathan@strms.fr");

        taskManager.registerUser(romels);
        taskManager.registerUser(danil);
        taskManager.registerUser(kylan);
        taskManager.registerUser(nathan);

        // Tâches de démonstration (créées par Alice)
        try {
            Task t1 = new Task("T-001", "Concevoir le schéma BDD",
                    "Modéliser la base de données utilisateurs et tâches.",
                    PriorityLevel.HIGH, TaskCategory.RESEARCH,
                    LocalDate.now().plusDays(7));
            Task t2 = new Task("T-002", "Implémenter l'authentification",
                    "Page de connexion avec hash de mot de passe.",
                    PriorityLevel.MEDIUM, TaskCategory.FEATURE,
                    LocalDate.now().plusDays(14));
            Task t3 = new Task("T-003", "Intégrer auth + BDD",
                    "Brancher le module d'authentification sur la base.",
                    PriorityLevel.CRITICAL, TaskCategory.FEATURE,
                    LocalDate.now().plusDays(21));
            Task t4 = new Task("T-004", "Documentation technique",
                    "Rédiger la documentation des APIs.",
                    PriorityLevel.LOW, TaskCategory.DOCUMENTATION,
                    LocalDate.now().plusDays(30));
            Task t5 = new Task("T-005", "Corriger le bug du login",
                    "Bug : message d'erreur non affiché après échec.",
                    PriorityLevel.HIGH, TaskCategory.BUGFIX,
                    LocalDate.now().plusDays(3));

            taskManager.addTask(t1, romels);
            taskManager.addTask(t2, romels);
            taskManager.addTask(t3, romels);
            taskManager.addTask(t4, romels);
            taskManager.addTask(t5, romels);

            // Dépendances : T-002 dépend de T-001, T-003 dépend de T-002
            taskManager.addDependency("T-002", "T-001", romels);
            taskManager.addDependency("T-003", "T-002", romels);

            // Assignation de T-001 et T-005 à Kylan
            taskManager.assignTask("T-001", kylan.getId(), danil);
            taskManager.assignTask("T-005", kylan.getId(), danil);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
