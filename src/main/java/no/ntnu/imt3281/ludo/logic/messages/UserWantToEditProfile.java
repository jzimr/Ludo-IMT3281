package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantToEditProfile extends Message {

    String userid;
    String displayname;
    String imagestring;
    String password;

    public UserWantToEditProfile(String action, String userid, String displayname, String imagestring, String password){
        super(action);
        this.userid = userid;
        this.displayname = displayname;
        this.imagestring = imagestring;
        this.password = password;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public void setImageString(String imagestring) {
        this.imagestring = imagestring;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserid() {
        return userid;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getImageString() {
        return imagestring;
    }

    public String getPassword() {
        return password;
    }
}
