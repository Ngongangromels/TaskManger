package fr.eseo.strms;

import fr.eseo.strms.enums.PriorityLevel;
import fr.eseo.strms.enums.TaskCategory;
import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.exceptions.*;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires couvrant les scénarios obligatoires du cahier des charges
 * (section 7.2 - Mandatory Test Scenarios) :
 *  - Ajout valide d'une dépendance
 *  - Rejet d'une dépendance circulaire
 *  - Maintien de l'intégrité du graphe après rejet
 *  - Levée des exceptions appropriées
 *  - Suppression correcte de dépendances
 *
 * Couvre également : permissions par rôle, transitions de statut, blocage par dépendance.
 */
class TaskManagerTest {

    private TaskManager manager;
    private Admin admin;
    private Manager managerUser;
    private Engineer engineer;

    @BeforeEach
    void setUp() {
        manager = new TaskManager();
        admin = new Admin("U-A", "Alice", "alice@strms.fr");
        managerUser = new Manager("U-M", "Bob", "bob@strms.fr");
        engineer = new Engineer("U-E", "Charlie", "charlie@strms.fr");
        manager.registerUser(admin);
        manager.registerUser(managerUser);
        manager.registerUser(engineer);
    }

    private Task makeTask(String id, PriorityLevel prio) {
        return new Task(id, "Tâche " + id, "Description " + id,
                prio, TaskCategory.FEATURE, LocalDate.now().plusDays(7));
    }

    // =============== Création / suppression / permissions ===============

    @Test
    @DisplayName("Un admin peut créer une tâche")
    void admin_can_create_task() throws Exception {
        Task t = makeTask("T-1", PriorityLevel.HIGH);
        manager.addTask(t, admin);
        assertEquals(1, manager.getAllTasks().size());
        assertEquals(TaskStatus.TODO, t.getStatus());
        assertFalse(t.getHistory().isEmpty(), "Historique de création présent");
    }

    @Test
    @DisplayName("Un engineer ne peut pas créer une tâche (InvalidRoleException)")
    void engineer_cannot_create_task() {
        Task t = makeTask("T-1", PriorityLevel.LOW);
        assertThrows(InvalidRoleException.class, () -> manager.addTask(t, engineer));
    }

    @Test
    @DisplayName("Création de deux tâches avec le même id => DuplicateTaskException")
    void duplicate_task_throws() throws Exception {
        manager.addTask(makeTask("T-1", PriorityLevel.LOW), admin);
        Task duplicate = makeTask("T-1", PriorityLevel.HIGH);
        assertThrows(DuplicateTaskException.class, () -> manager.addTask(duplicate, admin));
    }

    @Test
    @DisplayName("Recherche d'une tâche inexistante => TaskNotFoundException")
    void task_not_found() {
        assertThrows(TaskNotFoundException.class, () -> manager.findTask("T-INEXISTANT"));
    }

    @Test
    @DisplayName("Suppression d'une tâche par l'admin")
    void admin_can_delete_task() throws Exception {
        Task t = makeTask("T-1", PriorityLevel.LOW);
        manager.addTask(t, admin);
        manager.deleteTask("T-1", admin);
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    @DisplayName("Un manager ne peut pas supprimer une tâche")
    void manager_cannot_delete_task() throws Exception {
        manager.addTask(makeTask("T-1", PriorityLevel.LOW), admin);
        assertThrows(InvalidRoleException.class,
                () -> manager.deleteTask("T-1", managerUser));
    }

    // =============== Dépendances : ajout valide ===============

    @Test
    @DisplayName("Scénario obligatoire #1 : Ajout réussi d'une dépendance valide")
    void add_valid_dependency_succeeds() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);

        manager.addDependency("T-2", "T-1", admin);

        assertEquals(1, t2.getDependencies().size());
        assertTrue(t2.getDependencies().contains(t1));
        assertEquals(TaskStatus.BLOCKED, t2.getStatus(), "T-2 bloquée tant que T-1 n'est pas DONE");
    }

    // =============== Dépendances : rejet de cycle ===============

    @Test
    @DisplayName("Scénario obligatoire #2 : Rejet d'une dépendance circulaire directe")
    void direct_circular_dependency_is_rejected() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);

        manager.addDependency("T-2", "T-1", admin); // T-2 -> T-1

        // Tentative : T-1 -> T-2  (cycle direct)
        assertThrows(CircularDependencyException.class,
                () -> manager.addDependency("T-1", "T-2", admin));
    }

    @Test
    @DisplayName("Scénario obligatoire #2 bis : Rejet d'une dépendance circulaire indirecte")
    void indirect_circular_dependency_is_rejected() throws Exception {
        Task a = makeTask("A", PriorityLevel.HIGH);
        Task b = makeTask("B", PriorityLevel.HIGH);
        Task c = makeTask("C", PriorityLevel.HIGH);
        manager.addTask(a, admin);
        manager.addTask(b, admin);
        manager.addTask(c, admin);

        manager.addDependency("B", "A", admin); // B -> A
        manager.addDependency("C", "B", admin); // C -> B

        // Tentative : A -> C produirait A -> C -> B -> A (cycle)
        assertThrows(CircularDependencyException.class,
                () -> manager.addDependency("A", "C", admin));
    }

    @Test
    @DisplayName("Une tâche ne peut pas dépendre d'elle-même")
    void self_dependency_is_rejected() throws Exception {
        manager.addTask(makeTask("T-1", PriorityLevel.HIGH), admin);
        assertThrows(CircularDependencyException.class,
                () -> manager.addDependency("T-1", "T-1", admin));
    }

    // =============== Maintien de l'intégrité du graphe après rejet ===============

    @Test
    @DisplayName("Scénario obligatoire #3 : Le graphe reste intact après le rejet d'un cycle")
    void graph_integrity_preserved_after_rejection() throws Exception {
        Task a = makeTask("A", PriorityLevel.HIGH);
        Task b = makeTask("B", PriorityLevel.HIGH);
        Task c = makeTask("C", PriorityLevel.HIGH);
        manager.addTask(a, admin);
        manager.addTask(b, admin);
        manager.addTask(c, admin);

        manager.addDependency("B", "A", admin);
        manager.addDependency("C", "B", admin);

        // Tentative qui doit être rejetée
        assertThrows(CircularDependencyException.class,
                () -> manager.addDependency("A", "C", admin));

        // Le graphe n'a pas changé
        assertEquals(1, b.getDependencies().size());
        assertEquals(1, c.getDependencies().size());
        assertEquals(0, a.getDependencies().size());
        assertTrue(b.getDependencies().contains(a));
        assertTrue(c.getDependencies().contains(b));
    }

    // =============== Suppression de dépendance ===============

    @Test
    @DisplayName("Scénario obligatoire #5 : Suppression correcte d'une dépendance")
    void remove_dependency_works() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);
        manager.addDependency("T-2", "T-1", admin);
        assertEquals(TaskStatus.BLOCKED, t2.getStatus());

        manager.removeDependency("T-2", "T-1", admin);

        assertTrue(t2.getDependencies().isEmpty());
        assertEquals(TaskStatus.TODO, t2.getStatus(),
                "Sans dépendance, la tâche redevient TODO/prête");
    }

    // =============== Cycle de vie : assignation, démarrage, complétion ===============

    @Test
    @DisplayName("Un manager peut assigner une tâche à un ingénieur")
    void manager_can_assign_task() throws Exception {
        Task t = makeTask("T-1", PriorityLevel.HIGH);
        manager.addTask(t, admin);
        manager.assignTask("T-1", engineer.getId(), managerUser);
        assertEquals(engineer, t.getAssignedEngineer());
        // Pas de dépendance => passe en IN_PROGRESS
        assertEquals(TaskStatus.IN_PROGRESS, t.getStatus());
    }

    @Test
    @DisplayName("Un ingénieur ne peut pas assigner une tâche")
    void engineer_cannot_assign_task() throws Exception {
        manager.addTask(makeTask("T-1", PriorityLevel.HIGH), admin);
        assertThrows(InvalidRoleException.class,
                () -> manager.assignTask("T-1", engineer.getId(), engineer));
    }

    @Test
    @DisplayName("Tentative de démarrer une tâche avec dépendance non terminée => DependencyNotCompletedException")
    void cannot_start_task_with_unfinished_dependency() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);
        manager.addDependency("T-2", "T-1", admin);
        manager.assignTask("T-2", engineer.getId(), managerUser);

        assertThrows(DependencyNotCompletedException.class,
                () -> manager.startTask("T-2", engineer));
    }

    @Test
    @DisplayName("Quand T-1 est terminée, T-2 peut être démarrée puis terminée (par Manager/Admin)")
    void task_unblocks_when_dependency_done() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);
        manager.addDependency("T-2", "T-1", admin);

        // Assigne T-1 et T-2 à l'ingénieur
        manager.assignTask("T-1", engineer.getId(), managerUser);
        manager.assignTask("T-2", engineer.getId(), managerUser);

        // T-1 démarre déjà en IN_PROGRESS via assignTask.
        // Seul le manager (ou l'admin) peut désormais clôturer.
        manager.completeTask("T-1", managerUser);
        assertEquals(TaskStatus.DONE, t1.getStatus());

        // Maintenant T-2 peut être démarrée par l'ingénieur
        manager.startTask("T-2", engineer);
        assertEquals(TaskStatus.IN_PROGRESS, t2.getStatus());
        // Et terminée par le manager
        manager.completeTask("T-2", managerUser);
        assertEquals(TaskStatus.DONE, t2.getStatus());
    }

    @Test
    @DisplayName("Une tâche DONE est terminale (transition impossible)")
    void done_is_terminal() throws Exception {
        Task t = makeTask("T-1", PriorityLevel.HIGH);
        manager.addTask(t, admin);
        manager.assignTask("T-1", engineer.getId(), managerUser);
        // L'admin termine la tâche
        manager.completeTask("T-1", admin);

        assertThrows(InvalidTaskStateException.class,
                () -> t.updateStatus(TaskStatus.IN_PROGRESS));
    }

    // =============== Nouvelle règle métier : qui peut TERMINER une tâche ===============

    @Test
    @DisplayName("L'admin peut terminer une tâche")
    void admin_can_complete_task() throws Exception {
        Task t = makeTask("T-1", PriorityLevel.HIGH);
        manager.addTask(t, admin);
        manager.assignTask("T-1", engineer.getId(), managerUser);
        manager.completeTask("T-1", admin);
        assertEquals(TaskStatus.DONE, t.getStatus());
    }

    @Test
    @DisplayName("Le manager peut terminer une tâche")
    void manager_can_complete_task() throws Exception {
        Task t = makeTask("T-1", PriorityLevel.HIGH);
        manager.addTask(t, admin);
        manager.assignTask("T-1", engineer.getId(), managerUser);
        manager.completeTask("T-1", managerUser);
        assertEquals(TaskStatus.DONE, t.getStatus());
    }

    @Test
    @DisplayName("L'ingénieur ne peut PAS terminer une tâche (InvalidRoleException)")
    void engineer_cannot_complete_task() throws Exception {
        Task t = makeTask("T-1", PriorityLevel.HIGH);
        manager.addTask(t, admin);
        manager.assignTask("T-1", engineer.getId(), managerUser);
        // Même l'ingénieur assigné ne peut plus clôturer la tâche
        assertThrows(InvalidRoleException.class,
                () -> manager.completeTask("T-1", engineer));
        assertEquals(TaskStatus.IN_PROGRESS, t.getStatus(),
                "Le statut doit rester inchangé après le rejet.");
    }

    @Test
    @DisplayName("L'ingénieur peut toujours DÉMARRER une tâche qui lui est assignée")
    void engineer_can_still_start_task() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);
        manager.addDependency("T-2", "T-1", admin);
        manager.assignTask("T-1", engineer.getId(), managerUser);
        manager.assignTask("T-2", engineer.getId(), managerUser);
        manager.completeTask("T-1", admin);

        // T-2 est BLOCKED puis débloquée — l'ingénieur peut la démarrer.
        manager.startTask("T-2", engineer);
        assertEquals(TaskStatus.IN_PROGRESS, t2.getStatus());
    }

    @Test
    @DisplayName("Permissions : Admin/Manager renvoient true pour canCompleteTask, Engineer false")
    void can_complete_task_permissions() {
        assertTrue(admin.canCompleteTask(),       "Admin doit pouvoir terminer");
        assertTrue(managerUser.canCompleteTask(), "Manager doit pouvoir terminer");
        assertFalse(engineer.canCompleteTask(),   "Engineer ne doit PAS pouvoir terminer");
    }

    @Test
    @DisplayName("Suppression d'une tâche : retire la dépendance pour les tâches qui en dépendaient")
    void delete_task_removes_dangling_dependencies() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);
        manager.addDependency("T-2", "T-1", admin);

        manager.deleteTask("T-1", admin);

        assertTrue(t2.getDependencies().isEmpty());
        assertEquals(TaskStatus.TODO, t2.getStatus());
    }

    // =============== Priorité ===============

    @Test
    @DisplayName("Tri par priorité : la tâche CRITICAL passe avant la LOW")
    void priority_queue_orders_correctly() throws Exception {
        Task low = makeTask("LOW", PriorityLevel.LOW);
        Task crit = makeTask("CRIT", PriorityLevel.CRITICAL);
        Task med = makeTask("MED", PriorityLevel.MEDIUM);
        manager.addTask(low, admin);
        manager.addTask(crit, admin);
        manager.addTask(med, admin);

        Task next = manager.peekNextReadyTask();
        assertEquals("CRIT", next.getId(),
                "La tâche critique doit être en tête de la file");
    }

    @Test
    @DisplayName("Comparaison directe via Comparable")
    void task_comparable_orders_by_priority_desc() {
        Task low = makeTask("L", PriorityLevel.LOW);
        Task high = makeTask("H", PriorityLevel.HIGH);
        assertTrue(high.compareTo(low) < 0,
                "HIGH doit être considéré 'plus petit' (=plus prioritaire)");
    }

    // =============== Historique ===============

    @Test
    @DisplayName("L'ajout d'une dépendance laisse une trace dans l'historique")
    void dependency_addition_logs_history() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);

        int sizeBefore = t2.getHistory().size();
        manager.addDependency("T-2", "T-1", admin);

        assertEquals(sizeBefore + 1, t2.getHistory().size());
        assertEquals("DEPENDENCY_ADDED",
                t2.getHistory().get(t2.getHistory().size() - 1).getAction());
    }

    @Test
    @DisplayName("Le rejet d'un cycle laisse une trace dans l'historique de la tâche cible")
    void cycle_rejection_is_logged() throws Exception {
        Task t1 = makeTask("T-1", PriorityLevel.HIGH);
        Task t2 = makeTask("T-2", PriorityLevel.HIGH);
        manager.addTask(t1, admin);
        manager.addTask(t2, admin);
        manager.addDependency("T-2", "T-1", admin);

        try {
            manager.addDependency("T-1", "T-2", admin);
        } catch (CircularDependencyException expected) { /* attendu */ }

        boolean found = t1.getHistory().stream()
                .anyMatch(h -> "DEPENDENCY_REJECTED".equals(h.getAction()));
        assertTrue(found, "Une entrée DEPENDENCY_REJECTED doit être présente");
    }
}
