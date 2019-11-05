package no.ntnu.imt3281.ludo.logic.messages;

public class ErrorMessageResponse extends Message {
    String message;

    public ErrorMessageResponse(String action) {super(action);}

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
