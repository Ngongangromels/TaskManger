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

        historyTask.add(new HistoryEntry("task " + ID + " is created ","sys"));
    }

    public void addHistoryEntry(HistoryEntry entry){
        historyTask.add(entry);
    }

    public void updateState(PriorityLevel priority){
        priorityLevel = priority;
    }
    
    public boolean moreImportantThan(Task other){
        if (this.priorityLevel.ordinal() > other.priorityLevel.ordinal()) return true;
        else return false;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder("[");

        for (int i = 0; i < historyTask.size();i++){
            HistoryEntry t = historyTask.get(i);
            str.append("Task { action: ").append(t.getAction()).append(", modified: ").append(t.getTimestamp()).append(" }");
            if (i<historyTask.size()-1) str.append(", ");
        }
        str.append("]");
        return "Task: " + ID + " with title: " + title+ "\n" + "history: "+ str + " \n";
    }
}
