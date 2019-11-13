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

    public String getGameid() {
        return gameid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public int getPlayerid() {
        return playerid;
    }

    public void setPlayerid(int playerid) {
        this.playerid = playerid;
    }

    public int getPiecemoved() {
        return piecemoved;
    }

    public void setPiecemoved(int piecemoved) {
        this.piecemoved = piecemoved;
    }

    public int getMovedfrom() {
        return movedfrom;
    }

    public void setMovedfrom(int movedfrom) {
        this.movedfrom = movedfrom;
    }

    public int getMovedto() {
        return movedto;
    }

    public void setMovedto(int movedto) {
        this.movedto = movedto;
    }
}
