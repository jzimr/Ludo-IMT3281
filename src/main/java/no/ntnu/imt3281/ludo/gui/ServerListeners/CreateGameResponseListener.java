package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.CreateGameResponse;

public interface CreateGameResponseListener {
    void createGameResponseEvent(CreateGameResponse response);
}
