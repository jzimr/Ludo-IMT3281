package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.SentMessageResponse;

public interface SentMessageResponseListener extends EqualsChatRoomId{
    void sentMessageResponseEvent(SentMessageResponse response);
}
