package no.ntnu.imt3281.ludo.logic;

/**
 * Interface implementing a listener for when a dice is thrown
 */
public interface DiceListener {

    /**
     * Called when a dice is thrown.
     * @param diceEvent returns data about dice rolled
     */
    void diceThrown(DiceEvent diceEvent);
}
