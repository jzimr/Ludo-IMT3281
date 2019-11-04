package no.ntnu.imt3281.ludo.logic.messages;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Empty parent class for specific Client-Server messages
 */
public class Message {
    public String action;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String recipientSessionId;

    public Message(String action){
        this.action = action;
    }

    public void setRecipientSessionId(String recipientSessionId) {
        this.recipientSessionId = recipientSessionId;
    }

    public String getRecipientSessionId() {
        return recipientSessionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
