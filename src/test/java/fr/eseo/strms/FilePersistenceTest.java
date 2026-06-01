package fr.eseo.strms;

import fr.eseo.strms.enums.PriorityLevel;
import fr.eseo.strms.enums.TaskCategory;
import fr.eseo.strms.exceptions.FilePersistenceException;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Admin;
import fr.eseo.strms.model.Engineer;
import fr.eseo.strms.model.Task;
import fr.eseo.strms.utils.FileManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration sur la persistance fichier.
 * Vérifient le cycle save -> load -> sauve les mêmes données.
 */
class FilePersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Sauvegarde puis chargement préserve les tâches et dépendances")
    void save_then_load_round_trip() throws Exception {
        TaskManager m1 = new TaskManager();
        Admin admin = new Admin("U-A", "Alice", "alice@strms.fr");
        Engineer eng = new Engineer("U-E", "Charlie", "charlie@strms.fr");
        m1.registerUser(admin);
        m1.registerUser(eng);

        Task t1 = new Task("T-1", "Tâche 1", "desc 1",
                PriorityLevel.HIGH, TaskCategory.FEATURE, LocalDate.of(2026, 6, 1));
        Task t2 = new Task("T-2", "Tâche 2", "desc 2",
                PriorityLevel.MEDIUM, TaskCategory.BUGFIX, LocalDate.of(2026, 6, 15));
        m1.addTask(t1, admin);
        m1.addTask(t2, admin);
        m1.addDependency("T-2", "T-1", admin);
        m1.assignTask("T-1", eng.getId(), admin); // admin peut aussi assigner

        FileManager fm = new FileManager();
        Path file = tempDir.resolve("save.strms");
        fm.saveTasksToFile(m1, file.toString());

        // Recharge dans un nouveau manager
        TaskManager m2 = new TaskManager();
        m2.registerUser(admin);
        m2.registerUser(eng);
        fm.loadTasksFromFile(m2, file.toString());

        assertEquals(2, m2.getAllTasks().size());
        Task reloaded2 = m2.findTask("T-2");
        assertEquals(1, reloaded2.getDependencies().size());
        assertEquals("T-1", reloaded2.getDependencies().get(0).getId());

        Task reloaded1 = m2.findTask("T-1");
        assertNotNull(reloaded1.getAssignedEngineer());
        assertEquals("U-E", reloaded1.getAssignedEngineer().getId());
    }

    @Test
    @DisplayName("Charger un fichier inexistant lève FilePersistenceException")
    void loading_missing_file_throws() {
        FileManager fm = new FileManager();
        TaskManager m = new TaskManager();
        assertThrows(FilePersistenceException.class,
                () -> fm.loadTasksFromFile(m, tempDir.resolve("missing.strms").toString()));
    }
}
