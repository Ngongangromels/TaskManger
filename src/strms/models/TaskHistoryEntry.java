package strms.models;

import java.time.LocalDateTime;

public class TaskHistoryEntry {
    private  final LocalDateTime timestamp;
    private final User performedBy;        // [cite: 310]
    private final String action;           // [cite: 172]
    private final String description;

    public TaskHistoryEntry(User performedBy, String action, String description) {
        this.timestamp = LocalDateTime.now();
        this.performedBy = performedBy;
        this.action = action;
        this.description = description;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + performedBy.getId() + " - " + action + ": " + description;
    }

}
