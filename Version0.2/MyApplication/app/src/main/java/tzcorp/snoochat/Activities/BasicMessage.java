package tzcorp.snoochat.Activities;

/**
 * Created by tony on 05/06/17.
 */

public class BasicMessage {
    private String text;
    private String name;
    private String utcTime;

    public BasicMessage(){}

    public BasicMessage(final String text, final String name, final String utcTime){
        this.name = name;
        this.text = text;
        this.utcTime = utcTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(String utcTime) {
        this.utcTime = utcTime;
    }
}
