package no.ntnu.imt3281.ludo.logic.messages;

public class PieceMovedResponse extends Message {

    String gameid;
    int playerid;
    int piecemoved;
    int movedfrom;
    int movedto;

    public PieceMovedResponse(String action){super(action);}

    public PieceMovedResponse(String action, String gameid, int playerid, int piecemoved, int movedfrom, int movedto){
        super(action);
        this.playerid = playerid;
        this.piecemoved = piecemoved;
        this.movedfrom = movedfrom;
        this.movedto = movedto;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setMovedto(int movedto) {
        this.movedto = movedto;
    }

    public int getMovedto() {
        return movedto;
    }

    public void setMovedfrom(int movedfrom) {
        this.movedfrom = movedfrom;
    }

    public int getMovedfrom() {
        return movedfrom;
    }

    public void setPlayerid(int playerid) {
        this.playerid = playerid;
    }

    public int getPlayerid() {
        return playerid;
    }

    public void setPiecemoved(int piecemoved) {
        this.piecemoved = piecemoved;
    }

    public int getPiecemoved() {
        return piecemoved;
    }
}
