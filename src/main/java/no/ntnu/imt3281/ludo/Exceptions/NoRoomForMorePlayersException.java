package no.ntnu.imt3281.ludo.Exceptions;

/**
 * When there is no more room for a player in a Ludo lobby
 */
public class NoRoomForMorePlayersException extends RuntimeException {
    public NoRoomForMorePlayersException(String message){
        super(message);
    }
}
