package no.ntnu.imt3281.ludo.logic.messages;

public class UserHasConnected extends Message{
    public String username;

    public UserHasConnected(String action){super(action);}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
