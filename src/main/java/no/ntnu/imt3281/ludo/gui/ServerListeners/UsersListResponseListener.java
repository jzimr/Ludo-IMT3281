package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.UsersListResponse;

public interface UsersListResponseListener {
    void usersListResponseEvent(UsersListResponse response);
}
