package no.ntnu.imt3281.ludo.logic.messages;

public class UserHasConnected extends Message{
    public String username;
    public int userid;

    public UserHasConnected(String action){super(action);}

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getUserid() {
        return userid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
