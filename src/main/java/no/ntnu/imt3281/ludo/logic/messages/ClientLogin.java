package no.ntnu.imt3281.ludo.logic.messages;

public class ClientLogin extends Message {
    private String username;
    private String password;

    public ClientLogin(String action, String username, String password){
        super(action);
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
}
