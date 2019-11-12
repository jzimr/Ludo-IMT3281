package no.ntnu.imt3281.ludo.logic.messages;

public class GameHasStartedResponse extends Message {

    String gameid;

    public GameHasStartedResponse(String action){super(action);}

    public String getGameid() {
        return gameid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }
}
