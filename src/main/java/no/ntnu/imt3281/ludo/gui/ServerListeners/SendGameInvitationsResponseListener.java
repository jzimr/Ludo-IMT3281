package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.SendGameInvitationsResponse;

public interface SendGameInvitationsResponseListener {
    void sendGameInvitationsResponseEvent(SendGameInvitationsResponse response);
}
