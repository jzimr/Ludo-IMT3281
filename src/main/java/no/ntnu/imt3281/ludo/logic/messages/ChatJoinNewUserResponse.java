package no.ntnu.imt3281.ludo.logic.messages;

public class ChatJoinNewUserResponse extends Message {

    String userid;

    public ChatJoinNewUserResponse(String action) {super(action);}

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }
}
