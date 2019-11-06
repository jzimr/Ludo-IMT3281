package no.ntnu.imt3281.ludo.logic.messages;

public class UserLeftChatRoom extends Message {

    String chatroomname;
    String userid;

    public UserLeftChatRoom(String action, String userid, String chatroomname){
        super(action);
        this.userid = userid;
        this.chatroomname = chatroomname;
    }

    public void setChatroomname(String chatroomname) {
        this.chatroomname = chatroomname;
    }

    public String getChatroomname() {
        return chatroomname;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }
}
