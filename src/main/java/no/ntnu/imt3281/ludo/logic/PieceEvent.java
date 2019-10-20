package no.ntnu.imt3281.ludo.logic;

public class PieceEvent {
    final Ludo ludoGame;
    final int playerID;
    final int diceRolled;

    /**
     * A diceEvent should contain the ludo object, who rolled the dice and which number was thrown
     * @param ludoGame ludo game object
     * @param playerID ID of player who rolled the dice
     * @param diceRolled which number the dice rolled
     */
    public PieceEvent(Ludo ludoGame, int playerID, int diceRolled){
        this.ludoGame = ludoGame;
        this.playerID = playerID;
        this.diceRolled = diceRolled;
    }

    @Override
    public boolean equals(Object obj) {
        // if we are comparing object to itself
        if(this == obj){
            return true;
        }

        if (obj instanceof PieceEvent) {
            PieceEvent otherDiceEvent = (PieceEvent) obj;

            // if all data matches
            if(ludoGame == otherDiceEvent.ludoGame
                    && playerID == otherDiceEvent.playerID
                    && diceRolled == otherDiceEvent.diceRolled){
                return true;
            }
        }

        return false;
    }
}
