package no.ntnu.imt3281.ludo.logic.messages;

public class SentMessageResponse extends Message {

    String userid;
    String chatroomname;
    String chatmessage;
    String timestamp;

    public SentMessageResponse(String action){super(action);}

    public SentMessageResponse(String action, String userid, String chatroomname, String chatmessage, String timestamp){
        super(action);
        this.userid = userid;
        this.chatroomname = chatroomname;
        this.chatmessage = chatmessage;
        this.timestamp = timestamp;
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

    public void setChatmessage(String chatmessage) {
        this.chatmessage = chatmessage;
    }

    public String getChatmessage() {
        return chatmessage;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
