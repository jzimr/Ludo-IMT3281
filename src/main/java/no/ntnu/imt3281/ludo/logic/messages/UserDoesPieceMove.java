package no.ntnu.imt3281.ludo.logic.messages;

public class UserDoesPieceMove extends Message {

    String gameid;
    int playerid;
    int piecemoved;
    int movedfrom;
    int movedto;

    public UserDoesPieceMove(String action, String gameid, int playerid, int piecemoved, int movedfrom, int movedto){
        super(action);
        this.gameid = gameid;
        this.playerid = playerid;
        this.piecemoved = piecemoved;
        this.movedfrom = movedfrom;
        this.movedto = movedto;
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

    public void setMovedfrom(int movedfrom) {
        this.movedfrom = movedfrom;
    }

    public int getMovedfrom() {
        return movedfrom;
    }

    public void setMovedto(int movedto) {
        this.movedto = movedto;
    }

    public int getMovedto() {
        return movedto;
    }
}
