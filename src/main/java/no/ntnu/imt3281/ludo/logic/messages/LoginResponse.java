package no.ntnu.imt3281.ludo.logic.messages;

public class LoginResponse extends Message {
    String reponse;
    boolean loginStatus;

    public LoginResponse(String action){super(action);}

    public void setLoginStatus(boolean loginStatus) {
        this.loginStatus = loginStatus;
    }

    public boolean isLoginStatus() {
        return loginStatus;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getReponse() {
        return reponse;
    }
}

