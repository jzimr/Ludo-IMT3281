package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantToViewProfile extends Message{

    String displayname;

    public UserWantToViewProfile(String action, String displayname){
        super(action);
        this.displayname = displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getDisplayname() {
        return displayname;
    }
}
