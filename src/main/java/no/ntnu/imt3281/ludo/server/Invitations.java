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
        System.out.println("We ++ bois " + id);
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
                setOneAccepted(i);
            }
        }
    }

    public boolean isEveryoneAccepted(){
        System.out.println(answered + " " + players.length);
        return answered == players.length;
    }

    public boolean getOnePlayerAccepted(int id){return accepted[id];}

    public String getOnePlayerName(int id){return players[id];}


}
