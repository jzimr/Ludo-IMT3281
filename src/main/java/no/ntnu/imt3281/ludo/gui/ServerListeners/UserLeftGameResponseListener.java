package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.UserLeftGameResponse;

public interface UserLeftGameResponseListener {
    void userLeftGameResponseEvent(UserLeftGameResponse response);
    boolean equals(String gameId);
}
