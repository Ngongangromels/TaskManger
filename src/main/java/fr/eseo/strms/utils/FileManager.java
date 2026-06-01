package fr.eseo.strms.utils;

import fr.eseo.strms.enums.PriorityLevel;
import fr.eseo.strms.enums.TaskCategory;
import fr.eseo.strms.enums.TaskStatus;
import fr.eseo.strms.exceptions.FilePersistenceException;
import fr.eseo.strms.manager.TaskManager;
import fr.eseo.strms.model.Engineer;
import fr.eseo.strms.model.Task;
import fr.eseo.strms.model.TaskHistoryEntry;
import fr.eseo.strms.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire de persistance fichier.
 *
 * Utilise un format texte simple (CSV-like) pour rester pédagogique :
 * chaque tâche est sérialisée sur une ligne, suivie de ses dépendances et de son historique.
 *
 * Format :
 *   TASK;id;titre;description;priorité;statut;catégorie;deadline;ingénieur
 *   DEP;taskId;dependsOnId
 *   HIST;taskId;timestamp;action;performedBy;description
 *
 * Les chaînes contenant ';' sont échappées par double-encodage en remplaçant ';' par '\;'.
 */
public class FileManager {

    private static final String SEP = ";";
    private static final String ESC_SEP = "\\;";
    private static final String PLACEHOLDER = "";  // utilisé pendant l'unsplit

    /**
     * Sauvegarde toutes les tâches du TaskManager dans un fichier texte.
     */
    public void saveTasksToFile(TaskManager manager, String filePath)
            throws FilePersistenceException {

        Path path = Paths.get(filePath);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            throw new FilePersistenceException(
                    "Impossible de créer le dossier parent : " + filePath, e);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Task t : manager.getAllTasks()) {
                writer.write(serializeTask(t));
                writer.newLine();
                for (Task dep : t.getDependencies()) {
                    writer.write("DEP" + SEP + t.getId() + SEP + dep.getId());
                    writer.newLine();
                }
                for (TaskHistoryEntry entry : t.getHistory()) {
                    writer.write("HIST" + SEP + t.getId() + SEP
                            + entry.getTimestamp() + SEP
                            + escape(entry.getAction()) + SEP
                            + escape(entry.getPerformedBy()) + SEP
                            + escape(entry.getDescription()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Erreur d'écriture dans le fichier : " + filePath, e);
        }
    }

    /**
     * Charge toutes les tâches depuis un fichier et les ajoute au TaskManager.
     * Les utilisateurs (engineers) doivent déjà être enregistrés dans le manager.
     */
    public void loadTasksFromFile(TaskManager manager, String filePath)
            throws FilePersistenceException {

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FilePersistenceException("Fichier introuvable : " + filePath);
        }

        Map<String, Task> loaded = new HashMap<>();
        // On lit toutes les lignes en deux passes : d'abord les TASK (pour
        // que toutes les tâches existent dans `loaded`), ensuite les DEP et
        // les HIST. C'est nécessaire car l'ordre des lignes dans le fichier
        // n'est pas garanti (HashMap.values() est non ordonné côté écriture)
        // et une dépendance peut référencer une tâche déclarée plus loin.
        java.util.List<String[]> deferredLines = new java.util.ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = split(line);
                if ("TASK".equals(parts[0])) {
                    Task t = deserializeTask(parts, manager);
                    loaded.put(t.getId(), t);
                } else {
                    // Lignes DEP ou HIST : à traiter après le chargement de toutes les tâches
                    deferredLines.add(parts);
                }
            }
        } catch (IOException e) {
            throw new FilePersistenceException("Erreur de lecture du fichier : " + filePath, e);
        } catch (Exception e) {
            throw new FilePersistenceException(
                    "Fichier corrompu ou format invalide : " + filePath, e);
        }

        // Deuxième passe : maintenant que toutes les tâches sont chargées,
        // on peut résoudre les dépendances et les entrées d'historique.
        try {
            for (String[] parts : deferredLines) {
                switch (parts[0]) {
                    case "DEP" -> {
                        Task t = loaded.get(parts[1]);
                        Task dep = loaded.get(parts[2]);
                        if (t != null && dep != null) {
                            t.addDependency(dep);
                        }
                    }
                    case "HIST" -> {
                        Task t = loaded.get(parts[1]);
                        if (t != null) {
                            LocalDateTime ts = LocalDateTime.parse(parts[2]);
                            t.addHistoryEntry(new TaskHistoryEntry(
                                    parts[3], parts[4], parts[5], ts));
                        }
                    }
                    default -> { /* ligne inconnue, ignorée */ }
                }
            }
        } catch (Exception e) {
            throw new FilePersistenceException(
                    "Fichier corrompu ou format invalide : " + filePath, e);
        }

        // Enregistrer les tâches dans le manager (en évitant addTask qui exige un acteur)
        for (Task t : loaded.values()) {
            manager.injectTaskFromFile(t);
        }
    }

    /**
     * Écrit un rapport (texte libre) dans un fichier.
     * Utile pour exporter le rapport généré par ReportGenerator.
     */
    public void writeReport(String content, String filePath) throws FilePersistenceException {
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new FilePersistenceException("Impossible d'écrire le rapport : " + filePath, e);
        }
    }

    // ============== Sérialisation interne ==============

    private String serializeTask(Task t) {
        return "TASK" + SEP + t.getId()
                + SEP + escape(t.getTitle())
                + SEP + escape(t.getDescription() != null ? t.getDescription() : "")
                + SEP + t.getPriority().name()
                + SEP + t.getStatus().name()
                + SEP + t.getCategory().name()
                + SEP + (t.getDeadline() != null ? t.getDeadline().toString() : "")
                + SEP + (t.getAssignedEngineer() != null ? t.getAssignedEngineer().getId() : "");
    }

    private Task deserializeTask(String[] parts, TaskManager manager) {
        String id = parts[1];
        String title = parts[2];
        String description = parts[3];
        PriorityLevel priority = PriorityLevel.valueOf(parts[4]);
        TaskStatus status = TaskStatus.valueOf(parts[5]);
        TaskCategory category = TaskCategory.valueOf(parts[6]);
        LocalDate deadline = parts[7].isEmpty() ? null : LocalDate.parse(parts[7]);
        String engineerId = parts[8];

        Task t = new Task(id, title, description, priority, category, deadline);
        // Restaurer le statut chargé depuis le fichier (constructeur initialise à TODO)
        t.forceStatus(status);

        if (!engineerId.isEmpty()) {
            User u = manager.getUser(engineerId);
            if (u instanceof Engineer eng) {
                t.setAssignedEngineer(eng);
            }
        }
        return t;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace(SEP, ESC_SEP);
    }

    /**
     * Découpe une ligne en respectant l'échappement \;.
     */
    private String[] split(String line) {
        // Remplace les ';' échappés par un placeholder, on split, puis on restaure.
        String safe = line.replace(ESC_SEP, PLACEHOLDER);
        String[] parts = safe.split(SEP, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replace(PLACEHOLDER, SEP);
        }
        return parts;
    }
}
