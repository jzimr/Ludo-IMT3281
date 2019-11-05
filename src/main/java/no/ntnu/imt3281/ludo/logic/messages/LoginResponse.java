package no.ntnu.imt3281.ludo.logic.messages;

public class LoginResponse extends Message {
    String response;
    boolean loginStatus;

    public LoginResponse(String action){super(action);}

    public LoginResponse(String action, String response, boolean loginStatus){
        super(action);
        this.response = response;
        this.loginStatus = loginStatus;
    }

    public void setLoginStatus(boolean loginStatus) {
        this.loginStatus = loginStatus;
    }

    public boolean isLoginStatus() {
        return loginStatus;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}

