package no.ntnu.imt3281.ludo.logic.messages;


public class LoginOrRegisterResponse extends Message{
    String message;
    boolean LoginOrRegisterStatus;

    public LoginOrRegisterResponse(String action){super(action);}

    public void setLoginOrRegisterStatus(boolean LoginOrRegisterStatus) {
        this.LoginOrRegisterStatus = LoginOrRegisterStatus;
    }

    public boolean isLoginOrRegisterStatus() {
        return LoginOrRegisterStatus;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
