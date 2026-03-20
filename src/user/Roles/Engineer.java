package user.Roles;

import user.*;
import task.*;

import java.util.ArrayList;
import java.util.List;

class Engineer extends User {

    private List<Task> assignedTasks;

    public Engineer(String usrName){
        super(usrName,Role.Engineer);
        this.assignedTasks = new ArrayList<>();
    }

    public List<Task> getTasks(){
        return this.assignedTasks;
    }
}
