package user;
import java.util.UUID;

public abstract class User {
    private
    Role role;
    String name;
    String userID;
    
    public User(String usrName,Role usrRole){
        this.name = usrName;
        this.role = usrRole; 
        this.userID = UUID.randomUUID().toString();
    }

    public Role getRole(){
        return this.role;
    }
}
