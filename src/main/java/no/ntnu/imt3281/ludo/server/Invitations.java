package no.ntnu.imt3281.ludo.server;

public class Invitations {

    private String gameid;
    private String[] players;
    private Boolean[] accepted;

    public Invitations(){}

    public void setOneAccepted(int id) {
        accepted[id] = true;
    }


    public void setAccepted(Boolean[] accepted) {
        this.accepted = accepted;
        for (int i = 1; i < accepted.length; i++){
            accepted[i] = false;
        }
    }

    public void setPlayers(String[] players) {
        this.players = players;
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
            System.out.println(players[i] + " " + displayname);
            if (players[i].contentEquals(displayname)){
                accepted[i] = pAccepted;
            }
        }
    }

    public boolean isEveryoneAccepted(){
        int countTrue = 0;
        for (int i = 0; i < accepted.length; i++) {
            if (accepted[i]) {
                countTrue++;
            }
        }
        System.out.println(countTrue);
        return countTrue == accepted.length;
    }

    public boolean getOnePlayerAccepted(int id){return accepted[id];}

    public String getOnePlayerName(int id){return players[id];}


}
