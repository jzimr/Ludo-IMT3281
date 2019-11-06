package no.ntnu.imt3281.ludo.logic.messages;

public class UserLeftChatRoomResponse extends Message {

    String chatroomname;
    String userid;


    public UserLeftChatRoomResponse(String action){super(action);}


    public void setChatroomname(String chatroomname) {
        this.chatroomname = chatroomname;
    }

    public String getChatroomname() {
        return chatroomname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
