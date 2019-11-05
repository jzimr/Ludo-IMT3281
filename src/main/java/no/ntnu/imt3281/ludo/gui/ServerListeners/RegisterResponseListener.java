package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.RegisterResponse;

public interface RegisterResponseListener {
    void registerResponseEvent(RegisterResponse response);
}
