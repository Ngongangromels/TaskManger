package task.domain;
import java.time.LocalDateTime;
import java.lang.String;

public class HistoryEntry {
    private
    LocalDateTime timestamp;
    String createdBy;
    String action;

    public HistoryEntry(String actionMade,String by){
        action = actionMade;
        createdBy = by;
        timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getAction() {
        return action;
    }

    public String getPerformedBy() {
        return createdBy;
    }

    @Override
    public String toString(){
        return "[ "+timestamp+" ]" + " | " + action + " | " + " by " + createdBy + "\n";
    }

}
