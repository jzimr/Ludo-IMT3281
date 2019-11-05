package no.ntnu.imt3281.ludo.logic.messages;

public class ChatJoinResponse extends Message{
    boolean status;
    String response;

    public ChatJoinResponse(String action) {super(action);}

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
}
