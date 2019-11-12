package no.ntnu.imt3281.ludo.server;

public class Invitations {

    private String gameid;
    private String[] players;
    private Boolean[] accepted;
    private int answered = 0;

    public Invitations(){}

    public void setOneAccepted(int id) {
        accepted[id] = true;
        answered++;
    }

    public void setOneDecliend(int id) {
        accepted[id] = false;
        answered++;
    }

    public void setOnePlayer(String name, int id){
        players[id] = name;
    }

    public void setAccepted(Boolean[] accepted) {
        this.accepted = accepted;
    }

    public void setPlayers(String[] players) {
        this.players = players;
    }

    public Boolean[] getAccepted() {
        return accepted;
    }

    public String[] getPlayers() {
        return players;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setOneUpdate(String displayname, boolean pAccepted){
        for (int i = 0; i < players.length; i++) {
            if (players[i].contentEquals(displayname)){
                accepted[i] = pAccepted;
                answered++;
            }
        }
    }

    public boolean isEveryoneAccepted(){
        return answered == players.length;
    }

    public boolean getOnePlayerAccepted(int id){return accepted[id];}

    public String getOnePlayerName(int id){return players[id];}


}
