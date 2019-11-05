package no.ntnu.imt3281.ludo.logic.messages;

public class SentMessageResponse extends Message {

    String userid;
    String chatroomname;
    String chatmessage;
    String timestmap;

    public SentMessageResponse(String action){super(action);}

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

    public void setTimestmap(String timestmap) {
        this.timestmap = timestmap;
    }

    public String getTimestmap() {
        return timestmap;
    }
}
