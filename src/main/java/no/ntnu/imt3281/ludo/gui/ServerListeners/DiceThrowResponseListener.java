package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.DiceThrowResponse;

public interface DiceThrowResponseListener extends EqualsGameId{
    void diceThrowResponseEvent(DiceThrowResponse response);
}
