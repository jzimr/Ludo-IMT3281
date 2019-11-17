package no.ntnu.imt3281.ludo.server;

/**
 * Data class to store top ten players who've played most games and won most games
 */
public class TopTenList {
    public static class PlayedEntry{
        private String playerName;  // displayname
        private int playedCount;

        public PlayedEntry(String playerName, int playedCount){
            this.playerName = playerName;
            this.playedCount = playedCount;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getPlayedCount() {
            return playedCount;
        }
    }
    public static class WonEntry{
        private String playerName;  // displayname
        private int wonCount;

        public WonEntry(String playerName, int wonCount){
            this.playerName = playerName;
            this.wonCount = wonCount;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getWonCount() {
            return wonCount;
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
