package no.ntnu.imt3281.ludo.logic.messages;

public class UserLeftChatRoomResponse extends Message {

    String chatroomname;
    String displayname;


    public UserLeftChatRoomResponse(String action){super(action);}


    public void setChatroomname(String chatroomname) {
        this.chatroomname = chatroomname;
    }

    public String getChatroomname() {
        return chatroomname;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }
}
