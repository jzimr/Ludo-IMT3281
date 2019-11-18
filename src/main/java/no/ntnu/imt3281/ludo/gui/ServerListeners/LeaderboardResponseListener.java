package no.ntnu.imt3281.ludo.gui.ServerListeners;

import no.ntnu.imt3281.ludo.logic.messages.LeaderboardResponse;

public interface LeaderboardResponseListener {
    void leaderboardResponseEvent(LeaderboardResponse response);
}
