import strms.enums.*;
import strms.models.Engineer;
import strms.models.TaskHistoryEntry;
import strms.models.User;

import java.util.ArrayList;
import java.util.List;

public class Task implements Comparable<Task> {
    private final String id;
    private String title;
    private TaskStatus status;
    private PriorityLevel priority;
    private Engineer assignedEngineer;

    // Câblage des dépendances et historique [cite: 145]
    private final List<Task> dependencies;
    private final List<TaskHistoryEntry> history;

    public Task(String id, String title, PriorityLevel priority, User creator) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = TaskStatus.TODO; // État initial [cite: 181]
        this.dependencies = new ArrayList<>();
        this.history = new ArrayList<>();

        addHistoryEntry(creator, "CREATION", "Tâche créée.");
    }

    public String getId() { return id; }
    public TaskStatus getStatus() { return status; }
    public List<Task> getDependencies() { return dependencies; }

    public void setStatus(TaskStatus newStatus, User user) {
        this.status = newStatus;
        addHistoryEntry(user, "STATUS_CHANGE", "Passage à l'état " + newStatus);
    }

    public void addDependency(Task prerequisite) {
        this.dependencies.add(prerequisite); // [cite: 55]
    }

    public void addHistoryEntry(User user, String action, String description) {
        this.history.add(new TaskHistoryEntry(user, action, description)); // [cite: 56, 57]
    }

    @Override
    public int compareTo(Task other) {
        return Integer.compare(other.priority.ordinal(), this.priority.ordinal());
    }
}
