package no.ntnu.imt3281.ludo.logic.messages;

public class RegisterResponse extends Message {

    String response;
    boolean registerStatus;

    public RegisterResponse(String action){super(action);}

    public RegisterResponse(String action, String response, boolean registerStatus){
        super(action);
        this.response = response;
        this.registerStatus = registerStatus;
    }

    public void setRegisterStatus(boolean registerStatus) {
        this.registerStatus = registerStatus;
    }

    public boolean isRegisterStatus() {
        return registerStatus;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

}
