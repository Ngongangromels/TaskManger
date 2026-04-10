package strms.models;

public abstract class User {
    protected String id;
    protected String name;
    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    // Définition des droits d'accès logiques (polymorphisme)
    public abstract boolean canCreateTask();
    public abstract boolean canAssignTask();
}
