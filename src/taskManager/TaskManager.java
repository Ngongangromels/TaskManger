package taskManager;
import user.*;
import user.Roles.*;
import task.*;
import task.domain.*;

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

    public boolean isAlreadyAssigned(Task task,Engineer engineer){
        if (task == null) return false;
        return engineer.getTasks().containsKey(task.ID);
    }

    public void assignTask(User usr,Engineer engineer,Task task){
        if (usr.getRole() == Role.Manager && engineer.getRole() == Role.Engineer){
            if (!isAlreadyAssigned(task,engineer)){
                engineer.getTasks().put(task.ID, task);
                modifyTaskStatus(engineer,task,TaskStatus.IN_PROGRESS);
            } else throw new RuntimeException("This task has been already assigned!");
        } else throw new RuntimeException("Impossible assign tasks!");
    }

    public void modifyTaskStatus(User usr,Task task,TaskStatus status){
        if (usr.getRole() == Role.Engineer){
            task.modifyTaskStatus(status);
        } else throw new RuntimeException("You have no permission to modify tasks!\n");
    }
}
