package no.ntnu.imt3281.ludo.server;

/**
 * Data class to store top ten players who've played most games and won most games
 */
public class TopTenList {
    public static class PlayedEntry{
        private String playerName;  // displayname
        private int playedCount;    // amount of played ludo games
        private int place;          // which place is he in the leaderboard

        public PlayedEntry(String playerName, int playedCount, int place){
            this.playerName = playerName;
            this.playedCount = playedCount;
            this.place = place;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getPlayedCount() {
            return playedCount;
        }

        public int getPlace() {
            return place;
        }
    }
    public static class WonEntry{
        private String playerName;  // displayname
        private int wonCount;       // amount of won ludo games
        private int place;          // which place is he in the leaderboard

        public WonEntry(String playerName, int wonCount, int place){
            this.playerName = playerName;
            this.wonCount = wonCount;
            this.place = place;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getWonCount() {
            return wonCount;
        }

        public int getPlace() {
            return place;
        }
    }

    private PlayedEntry[] playedEntries = new PlayedEntry[0];       // max 10 on list
    private WonEntry[] wonEntries = new WonEntry[0];                // max 10 on list

    public TopTenList(PlayedEntry[] played, WonEntry[] won){
        if(played != null)
            playedEntries = played;

        if(won != null)
            wonEntries = won;
    }

    public PlayedEntry[] getPlayedEntries() {
        return playedEntries;
    }

    public WonEntry[] getWonEntries() {
        return wonEntries;
    }
}
