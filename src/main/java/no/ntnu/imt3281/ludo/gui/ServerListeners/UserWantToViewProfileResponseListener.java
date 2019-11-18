package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.UserWantToViewProfileResponse;

public interface UserWantToViewProfileResponseListener {
    void userWantToViewProfileResponseEvent(UserWantToViewProfileResponse response);

    /**
     * If the particular listener is currently waiting to get a profile. If not, nothing will happen.
     * @return true if listener is currently waiting for a profile, false if not waiting
     */
    boolean waitingForProfile(String displayname);
}
