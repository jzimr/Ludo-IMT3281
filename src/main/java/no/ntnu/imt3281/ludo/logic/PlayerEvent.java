package no.ntnu.imt3281.ludo.logic;



public class PlayerEvent {
    final static public String WAITING = "Waiting";
    final static public String PLAYING = "Playing";
    final static public String LEFTGAME = "Leftgame";
    final static public String WON = "Won";
    
    final Ludo ludo;
    final int playerID;
    final String playerEvent;

    PlayerEvent(Ludo game, int player, String event){
        ludo = game;
        playerID = player;
        playerEvent = event;
    }

    @Override
    public boolean equals(Object obj) {
        // if we are comparing object to itself
        if(this == obj){
            return true;
        }

        if (obj instanceof PlayerEvent) {
            PlayerEvent otherPlayerEvent = (PlayerEvent) obj;

            // if all data matches
            if(ludo == otherPlayerEvent.ludo
                    && playerID == otherPlayerEvent.playerID
                    && playerEvent == otherPlayerEvent.playerEvent){
                return true;
            }
        }

        return false;
    }

    public int getPlayerID() {
        return playerID;
    }

    public Ludo getLudo() {
        return ludo;
    }

    public String getPlayerEvent() {
        return playerEvent;
    }
}
