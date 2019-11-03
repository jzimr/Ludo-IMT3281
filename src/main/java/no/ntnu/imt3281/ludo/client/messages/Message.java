package no.ntnu.imt3281.ludo.client.messages;

/**
 * Empty parent class for specific Client-Server messages
 */
public class Message {
    public String action;

    public Message(String action){
        this.action = action;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
