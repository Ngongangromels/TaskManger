package strms.models;

public class Admin extends User {
    public Admin(String id, String name) { super(id, name); }
    @Override
    public boolean canCreateTask() { return true; }   // [cite: 99]
    @Override
    public boolean canAssignTask() { return true; }
}
