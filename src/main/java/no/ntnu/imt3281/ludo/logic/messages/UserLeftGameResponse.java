package no.ntnu.imt3281.ludo.logic.messages;

public class UserLeftGameResponse extends Message{

    String displayname;
    String gameid;

    public UserLeftGameResponse(String action){super(action);}

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getDisplayname() {
        return displayname;
    }

}
