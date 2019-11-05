package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.LoginResponse;

public interface LoginResponseListener {
    void loginResponseEvent(LoginResponse response);
}
