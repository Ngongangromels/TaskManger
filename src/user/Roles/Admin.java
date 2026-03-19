package user.Roles;

import user.*;

class Admin extends User {
    public Admin(String usrName){
        super(usrName,Role.Admin);
    }
}
