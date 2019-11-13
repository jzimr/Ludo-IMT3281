package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.PieceMovedResponse;

public interface PieceMovedResponseListener extends EqualsGameId {
    void pieceMovedResponseEvent(PieceMovedResponse response);
}
