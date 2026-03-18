package task;
import task.domain.*;
import java.util.Vector;
import java.util.UUID;
public class Task {
    private
        String ID;
        String title;

        PriorityLevel priorityLevel;
        TaskStatus taskStatus;   
        TaskCategory taskCategory;
        NotificationType notificationType;
        Vector<Task> dependencies;
        Vector<HistoryEntry> historyTask;

    public Task(String taskTitle){
        ID = UUID.randomUUID().toString();
        title = taskTitle;

        historyTask = new Vector<>();
        dependencies = new Vector<>();

        historyTask.add(new HistoryEntry("task "+ID+" is created","sys"));
    }

    public void addHistoryEntry(HistoryEntry entry){
        historyTask.add(entry);
    }
}
