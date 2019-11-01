package no.ntnu.imt3281.ludo.client.messages;

import no.ntnu.imt3281.ludo.logic.JsonMessage;

public class ClientLogin extends Message {
    private String action;
    private String username;
    private String password;

    public ClientLogin(String action, String username, String password){
        this.action = action;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
