package no.ntnu.imt3281.ludo.logic.messages;


public class LoginOrRegisterResponse extends Message{
    boolean LoginOrRegisterStatus;

    public LoginOrRegisterResponse(String action){super(action);}

    public void setLoginOrRegisterStatus(boolean LoginOrRegisterStatus) {
        this.LoginOrRegisterStatus = LoginOrRegisterStatus;
    }

    public boolean isLoginOrRegisterStatus() {
        return LoginOrRegisterStatus;
    }

}
