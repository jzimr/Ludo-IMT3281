package no.ntnu.imt3281.ludo.logic.messages;

public class UserSentMessage extends Message {

    String userid;
    String chatroomname;
    String chatmessage;

    public UserSentMessage(String action, String userid, String chatroomname, String chatmessage) {
        super(action);
        this.userid = userid;
        this.chatroomname = chatroomname;
        this.chatmessage = chatmessage;
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

    public void setChatmessage(String chatmessage) {
        this.chatmessage = chatmessage;
    }

    public String getChatmessage() {
        return chatmessage;
    }
}
