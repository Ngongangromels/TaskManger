package taskManager;
import user.*;
import task.*;

import java.util.HashMap;

public class TaskManager {

    private HashMap<String,Task> tasks = new HashMap<>();

    public Task createTask(User usr,String taskTitle){
        if (usr.getRole() == Role.Admin){
            Task task = new Task(taskTitle);
            tasks.put(task.ID, task);
            return task;
        } else throw new RuntimeException("You have no permission to create tasks!\n");
    }

    public void deleteTask(User usr, Task task){
        if (usr.getRole() == Role.Admin) tasks.remove(task.ID);
        else throw new RuntimeException("You have no permission to delete tasks!\n");
    }

    public void modifyTaskStatus(User usr,Task task){
        if (usr.getRole() == Role.Engineer){
            
        } else throw new RuntimeException("You have no permission to modify tasks!\n");
    }

    
}
