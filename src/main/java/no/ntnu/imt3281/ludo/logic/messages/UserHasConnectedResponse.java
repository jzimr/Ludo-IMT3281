package no.ntnu.imt3281.ludo.logic.messages;

public class UserHasConnectedResponse extends Message{
    public String username;

    public UserHasConnectedResponse(String action){super(action);}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
