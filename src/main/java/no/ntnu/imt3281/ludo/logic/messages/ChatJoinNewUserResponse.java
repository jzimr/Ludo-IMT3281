package no.ntnu.imt3281.ludo.logic.messages;

public class ChatJoinNewUserResponse extends Message {

    String displayname;

    public ChatJoinNewUserResponse(String action) {super(action);}

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getDisplayname() {
        return displayname;
    }
}
