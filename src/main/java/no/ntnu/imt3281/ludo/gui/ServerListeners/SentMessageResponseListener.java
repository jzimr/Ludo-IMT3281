package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.SentMessageResponse;

public interface SentMessageResponseListener {
    boolean equals(String chatName);
    void sentMessageResponseEvent(SentMessageResponse response);
}
