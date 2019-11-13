package no.ntnu.imt3281.ludo.logic.messages;

public class LoginResponse extends Message {
    String response;
    boolean loginStatus;
    String userid;
    String displayname;

    public LoginResponse(String action){super(action);}

    public LoginResponse(String action, String response, boolean loginStatus, String userid, String displayname){
        super(action);
        this.response = response;
        this.loginStatus = loginStatus;
        this.userid = userid;
        this.displayname = displayname;
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

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getDisplayname() {
        return displayname;
    }
}

