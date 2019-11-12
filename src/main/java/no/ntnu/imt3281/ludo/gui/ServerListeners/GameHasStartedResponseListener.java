package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.GameHasStartedResponse;

public interface GameHasStartedResponseListener extends EqualsGameId{
    void gameHasStartedResponseEvent(GameHasStartedResponse response);
}
