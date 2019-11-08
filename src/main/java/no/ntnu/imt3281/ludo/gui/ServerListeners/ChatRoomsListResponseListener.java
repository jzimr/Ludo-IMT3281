package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.ChatRoomsListResponse;

public interface ChatRoomsListResponseListener {
    void chatRoomsListResponseEvent(ChatRoomsListResponse response);
}
