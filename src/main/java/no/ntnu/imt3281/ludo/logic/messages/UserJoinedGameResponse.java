package no.ntnu.imt3281.ludo.logic.messages;

public class UserJoinedGameResponse extends Message {

    String userid;
    String gameid;
    String[] playersinlobby;

    public UserJoinedGameResponse(String action) {super(action);}

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

    public String[] getPlayersinlobby() {
        return playersinlobby;
    }

    public void setPlayersinlobby(String[] playersinlobby) {
        this.playersinlobby = playersinlobby;
    }

    @Override
    public String toString() {
        return userid + " " + gameid;
    }
}
