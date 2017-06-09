package tzcorp.redditchat.Activities;

/**
 * Created by tony on 05/06/17.
 */

public class BasicMessage {
    private String text;
    private String name;
    private String photoUrl;

    public BasicMessage(){}

    public BasicMessage(final String text, final String name, final String photoUrl){
        this.name = name;
        this.text = text;
        this.photoUrl = photoUrl;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
