package no.ntnu.imt3281.ludo.logic.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginOrRegisterResponse {
    boolean loginStatus;

    public void setLoginStatus(boolean loginStatus) {
        this.loginStatus = loginStatus;
    }

    public boolean isLoginStatus() {
        return loginStatus;
    }

    LoginOrRegisterResponse(){}
}
