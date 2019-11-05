package no.ntnu.imt3281.ludo.logic.messages;

public class UserSentMessage extends Message {

    String userid;
    String chatroomname;
    String chatMessage;

    public UserSentMessage(String action, String userid, String chatroomname, String chatmessage) {
        super(action);
        this.userid = userid;
        this.chatroomname = chatroomname;
        this.chatMessage = chatmessage;
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

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public String getChatMessage() {
        return chatMessage;
    }
}
