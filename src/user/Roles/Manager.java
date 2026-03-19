package user.Roles;

import user.*;

class Manager extends User {
    public Manager(String usrName){
        super(usrName,Role.Manager);
    }
}
