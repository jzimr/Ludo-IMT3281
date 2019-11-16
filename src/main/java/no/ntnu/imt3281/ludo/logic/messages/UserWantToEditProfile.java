package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantToEditProfile extends Message {

    String displayname;
    String imagestring;
    String password;

    public UserWantToEditProfile(String action, String displayname, String imagestring, String password){
        super(action);
        this.displayname = displayname;
        this.imagestring = imagestring;
        this.password = password;
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
