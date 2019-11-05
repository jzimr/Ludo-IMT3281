package no.ntnu.imt3281.ludo.logic.messages;

public class UserJoinChat extends Message {

    String userid;
    String chatroomname;

    public UserJoinChat(String action, String chatroomname, String userid){
        super(action);
        this.userid = userid;
        this.chatroomname = chatroomname;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setChatroomname(String chatroomname) {
        this.chatroomname = chatroomname;
    }

    public String getChatroomname() {
        return chatroomname;
    }

}
