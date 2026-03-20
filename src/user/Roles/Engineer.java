package user.Roles;

import user.*;
import task.*;

import java.util.HashMap;

public class Engineer extends User {

    private HashMap<String,Task> assignedTasks;

    public Engineer(String usrName){
        super(usrName,Role.Engineer);
        this.assignedTasks = new HashMap<>();
    }

    public HashMap<String,Task> getTasks(){
        return this.assignedTasks;
    }
}
