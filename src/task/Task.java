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

    public void main(String taskTitle){

        ID = UUID.randomUUID().toString();
        title = taskTitle;

        historyTask = new Vector<>();
        dependencies = new Vector<>();

    }

    public void addHistoryEntry(HistoryEntry entry){
        historyTask.add(entry);
    }
}
