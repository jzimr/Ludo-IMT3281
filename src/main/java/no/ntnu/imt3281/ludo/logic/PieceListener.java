package no.ntnu.imt3281.ludo.logic;

/**
 * Interface implementing a listener for when a piece is moved
 */
public interface PieceListener {

    /**
     * Called when a player piece is moved.
     * @param pieceEvent returns data about the piece moved
     */
    void pieceMoved(PieceEvent pieceEvent);
}
