package no.ntnu.imt3281.ludo.logic.messages;

public class PlayerWonGameResponse extends Message {

    String gameid;
    int playerwonid;

    public PlayerWonGameResponse(String action){super(action);}

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public void setPlayerwonid(int playerwonid) {
        this.playerwonid = playerwonid;
    }

    public String getGameid() {
        return gameid;
    }

    public int getPlayerwonid() {
        return playerwonid;
    }
}
