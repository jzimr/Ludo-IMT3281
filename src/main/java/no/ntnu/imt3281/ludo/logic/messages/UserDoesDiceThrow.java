package no.ntnu.imt3281.ludo.logic.messages;


public class UserDoesDiceThrow extends Message {

    int playerid;
    String gameid;


    public UserDoesDiceThrow(String action, int playerid, String gameid){
        super(action);
        this.playerid = playerid;
        this.gameid = gameid;
    }


    public void setPlayerid(int playerid) {
        this.playerid = playerid;
    }

    public int getPlayerid() {
        return playerid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }
}

