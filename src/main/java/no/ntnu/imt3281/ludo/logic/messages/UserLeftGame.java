package no.ntnu.imt3281.ludo.logic.messages;

public class UserLeftGame extends Message {

    String gameid;

    public UserLeftGame(String action, String gameid){
        super(action);
        this.gameid = gameid;

    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }
}
