package no.ntnu.imt3281.ludo.logic;

/**
 * Event object containing data about the dice rolled
 */
public class DiceEvent {
    final Ludo ludoGame;
    final int playerID;         // ID of player rolled
    final int diceRolled;       // Dice number rolled

    /**
     * A diceEvent should contain the ludo object, who rolled the dice and which number was thrown
     * @param ludoGame ludo game object
     * @param playerID ID of player who rolled the dice
     * @param diceRolled which number the dice rolled
     */
    public DiceEvent(Ludo ludoGame, int playerID, int diceRolled){
        this.ludoGame = ludoGame;
        this.playerID = playerID;
        this.diceRolled = diceRolled;
    }

    public Ludo getLudoGame() {
        return ludoGame;
    }

    public int getDiceRolled() {
        return diceRolled;
    }

    public int getPlayerID() {
        return playerID;
    }

    @Override
    public boolean equals(Object obj) {
        // if we are comparing object to itself
        if(this == obj){
            return true;
        }

        if (obj instanceof DiceEvent) {
            DiceEvent otherDiceEvent = (DiceEvent) obj;

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
