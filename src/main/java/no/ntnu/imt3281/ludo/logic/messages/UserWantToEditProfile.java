package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantToEditProfile extends Message {

    String displayname;
    byte[] imagestring;
    String password;

    public UserWantToEditProfile(String action, String displayname, byte[] imagestring, String password){
        super(action);
        this.displayname = displayname;
        this.imagestring = imagestring;
        this.password = password;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setImageString(byte[] imagestring) {
        this.imagestring = imagestring;
    }

    public byte[] getImageString() {
        return imagestring;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
