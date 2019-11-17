package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.UserWantToEditProfileResponse;

public interface UserWantToEditProfileResponseListener {
    void userWantToEditProfileResponseEvent(UserWantToEditProfileResponse response);
}
