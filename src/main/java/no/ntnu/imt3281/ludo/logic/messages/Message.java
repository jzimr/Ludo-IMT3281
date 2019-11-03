package no.ntnu.imt3281.ludo.logic.messages;

/**
 * Empty parent class for specific Client-Server messages
 */
public class Message {
    // This is here temporary
    // TODO: Replace with correct information later when things start to be implemented.
    public String recipientUsername;
    public int recipientId;


    public String action;

    public Message(String action){
        this.action = action;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
