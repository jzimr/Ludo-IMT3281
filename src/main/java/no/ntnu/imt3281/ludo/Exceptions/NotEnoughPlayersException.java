package no.ntnu.imt3281.ludo.Exceptions;

/**
 * Throws when less than 2 players are added in a ludo game
 */
public class NotEnoughPlayersException extends RuntimeException  {
    public NotEnoughPlayersException(String message){
        super(message);
    }
}
