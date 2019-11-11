package no.ntnu.imt3281.ludo.logic.messages;

public class PlayerStateChangeResponse extends Message{

    String gameid;
    int activeplayerid;
    String playerstate;

    public PlayerStateChangeResponse(String action){super(action);}

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setActiveplayerid(int activeplayerid) {
        this.activeplayerid = activeplayerid;
    }

    public int getActiveplayerid() {
        return activeplayerid;
    }

    public void setPlayerstate(String playerstate) {
        this.playerstate = playerstate;
    }

    public String getPlayerstate() {
        return playerstate;
    }
}
