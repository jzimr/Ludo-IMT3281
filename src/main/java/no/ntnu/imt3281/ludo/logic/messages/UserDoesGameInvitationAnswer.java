package no.ntnu.imt3281.ludo.logic.messages;

public class UserDoesGameInvitationAnswer extends Message {


    boolean accepted;
    String userid;
    String gameid;

    public UserDoesGameInvitationAnswer(String action){super(action);}


    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }
}
