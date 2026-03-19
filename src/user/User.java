package user;
import java.util.UUID;

public abstract class User {
    private
    Role role;
    String name;
    String userID;
    
    public User(String usrName,Role usrRole){
        name = usrName;
        role = usrRole; 
        userID = UUID.randomUUID().toString();
    }
}
