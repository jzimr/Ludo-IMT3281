package no.ntnu.imt3281.ludo.logic.messages;

public class ChatJoin extends Message{
    boolean status;
    String response;

    public ChatJoin(String action) {super(action);}

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
