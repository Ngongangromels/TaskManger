package task;
import task.domain.*;
import java.util.Vector;
public class Task {
    private
        PriorityLevel priorityLevel;
        TaskStatus taskStatus;   
        TaskCategory taskCategory;
        NotificationType notificationType;
        Vector<Task> HistoryTask;

    public void main(){
        HistoryTask = new Vector<>();
    }
}
