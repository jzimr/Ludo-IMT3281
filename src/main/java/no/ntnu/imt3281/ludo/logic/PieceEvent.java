package no.ntnu.imt3281.ludo.logic;

/**
 * Event object containing data about the piece moved
 */
public class PieceEvent {
    final Ludo ludoGame;
    final int playerID;     // ID of player
    final int pieceMoved;   // ID of piece moved
    final int from;         // position moved from
    final int to;           // position moved to

    /**
     * A diceEvent should contain the ludo object, who rolled the dice and which number was thrown
     * @param ludoGame ludo game object
     * @param playerID ID of player who rolled the dice
     * @param pieceMoved which piece was moved
     * @param from which position moved from
     * @param to which position moved to
     */
    public PieceEvent(Ludo ludoGame, int playerID, int pieceMoved, int from, int to){
        this.ludoGame = ludoGame;
        this.playerID = playerID;
        this.pieceMoved = pieceMoved;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        // if we are comparing object to itself
        if(this == obj){
            return true;
        }

        if (obj instanceof PieceEvent) {
            PieceEvent otherPieceEvent = (PieceEvent) obj;

            // if all data matches
            if(ludoGame == otherPieceEvent.ludoGame
                    && playerID == otherPieceEvent.playerID
                    && pieceMoved == otherPieceEvent.pieceMoved
                    && from == otherPieceEvent.from
                    && to == otherPieceEvent.to){
                return true;
            }
        }

        return false;
    }

    public int getPlayerID() {
        return playerID;
    }

    public Ludo getLudoGame() {
        return ludoGame;
    }

    public int getFrom() {
        return from;
    }

    public int getPieceMoved() {
        return pieceMoved;
    }

    public int getTo() {
        return to;
    }
}
