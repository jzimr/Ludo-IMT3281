package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.UserJoinedGameResponse;

public interface UserJoinedGameResponseListener {
    void userJoinedGameResponseEvent(UserJoinedGameResponse response);
}
