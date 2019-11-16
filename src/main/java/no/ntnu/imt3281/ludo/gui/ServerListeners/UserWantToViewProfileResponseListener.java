package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.UserWantToViewProfileResponse;

public interface UserWantToViewProfileResponseListener {
    void userWantToViewProfileResponseEvent(UserWantToViewProfileResponse response);
}
