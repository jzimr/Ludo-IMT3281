package no.ntnu.imt3281.ludo.Exceptions;

/**
 * Exception when an image provided by the user is invalid
 */
public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String message){
        super(message);
    }
}
