package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.ChatJoinResponse;

public interface ChatJoinResponseListener {
    void chatJoinResponseEvent(ChatJoinResponse response);
}
