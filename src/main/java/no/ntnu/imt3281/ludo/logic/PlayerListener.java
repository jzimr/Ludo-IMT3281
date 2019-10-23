package no.ntnu.imt3281.ludo.logic;

/**
 * Interface implementing a listener for when a player changes state
 */
public interface PlayerListener {

    /**
     * Called when a players state changes
     * @param event PlayerEvent containing info about what state the player is in.
     */
    void playerStateChanged(PlayerEvent event);
}
