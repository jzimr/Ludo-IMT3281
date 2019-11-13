package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.UserLeftChatRoomResponse;

public interface UserLeftChatRoomResponseListener extends EqualsChatRoomId {
    void userLeftChatRoomResponseEvent(UserLeftChatRoomResponse response);
}
