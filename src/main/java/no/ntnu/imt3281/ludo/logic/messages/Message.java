package no.ntnu.imt3281.ludo.logic.messages;

/**
 * Empty parent class for specific Client-Server messages
 */
public class Message {
    public String recipientuuid;


    public String action;

    public Message(String action){
        this.action = action;
    }

    public void setrecipientuuid(String recipientuuid) {
        this.recipientuuid = recipientuuid;
    }

    public String getrecipientuuid() {
        return recipientuuid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
