package no.ntnu.imt3281.ludo.logic.messages;

public class UserLeftGameResponse extends Message{

    String userid;
    String gameid;

    public UserLeftGameResponse(String action){super(action);}

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }
}
