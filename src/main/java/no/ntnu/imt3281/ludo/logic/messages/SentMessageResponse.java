package no.ntnu.imt3281.ludo.logic.messages;

public class SentMessageResponse extends Message {

    String displayname;
    String chatroomname;
    String chatmessage;
    String timestamp;

    public SentMessageResponse(String action){super(action);}

    public SentMessageResponse(String action, String displayname, String chatroomname, String chatmessage, String timestamp){
        super(action);
        this.displayname = displayname;
        this.chatroomname = chatroomname;
        this.chatmessage = chatmessage;
        this.timestamp = timestamp;
    }

    public void setdisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getdisplayname() {
        return displayname;
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
