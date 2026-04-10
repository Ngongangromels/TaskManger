package strms.models;

public class Engineer extends User{
    public Engineer(String id, String name) { super(id, name); }
    @Override
    public boolean canCreateTask() { return false; }
    @Override
    public boolean canAssignTask() { return false; }
}
