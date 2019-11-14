package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantToViewProfile extends Message{

    String userid;

    public UserWantToViewProfile(String action, String userid){
        super(action);
        this.userid = userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }
}
