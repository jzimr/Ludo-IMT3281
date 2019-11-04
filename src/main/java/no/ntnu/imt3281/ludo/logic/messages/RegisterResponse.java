package no.ntnu.imt3281.ludo.logic.messages;

public class RegisterResponse extends Message {

    String reponse;
    boolean registerStatus;

    public RegisterResponse(String action){super(action);}

    public void setRegisterStatus(boolean registerStatus) {
        this.registerStatus = registerStatus;
    }

    public boolean isRegisterStatus() {
        return registerStatus;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getReponse() {
        return reponse;
    }

}
