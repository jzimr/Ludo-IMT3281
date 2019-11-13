package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.ChatJoinNewUserResponse;

public interface ChatJoinNewUserResponseListener extends EqualsChatRoomId{
    void chatJoinNewUserResponseEvent(ChatJoinNewUserResponse response);
}
