package no.ntnu.imt3281.ludo.logic.messages;

public class CreateGameResponse extends Message {

    String gameid;
    boolean joinstatus;
    String response;

    public CreateGameResponse(String action) {super(action);}

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setJoinstatus(boolean joinstatus) {
        this.joinstatus = joinstatus;
    }

    public boolean isJoinstatus() {
        return joinstatus;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
