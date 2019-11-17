package no.ntnu.imt3281.ludo.logic.messages;

import no.ntnu.imt3281.ludo.server.TopTenList;

public class LeaderboardResponse extends Message {

    TopTenList.PlayedEntry[] toptenplays;
    TopTenList.WonEntry[] toptenwins;

    public LeaderboardResponse(String action){super(action);}


    public void setToptenplays(TopTenList.PlayedEntry[] toptenplays) {
        this.toptenplays = toptenplays;
    }

    public void setToptenwins(TopTenList.WonEntry[] toptenwins) {
        this.toptenwins = toptenwins;
    }

    public TopTenList.PlayedEntry[] getToptenplays() {
        return toptenplays;
    }

    public TopTenList.WonEntry[] getToptenwins() {
        return toptenwins;
    }
}

