package no.ntnu.imt3281.ludo.logic.messages;

public class UserDeclinedGameInvitationResponse extends Message {

    boolean accepted = false;
    String userid;
    String gameid;

    public UserDeclinedGameInvitationResponse(String action){super(action);}

    public boolean isAccepted() {
        return accepted;
    }

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
