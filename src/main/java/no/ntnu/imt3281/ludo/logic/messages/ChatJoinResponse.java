package no.ntnu.imt3281.ludo.logic.messages;

import no.ntnu.imt3281.ludo.server.ChatMessage;

public class ChatJoinResponse extends Message{
    boolean status;
    String response;
    String chatroomname;
    String[] usersinroom;
    ChatMessage[] chatlog;

    public ChatJoinResponse(String action) {super(action);}

    public ChatJoinResponse(String action, boolean status, String response, String chatroomname, String[] usersinroom, ChatMessage[] chatlog){
        super(action);
        this.status = status;
        this.response = response;
        this.chatroomname = chatroomname;
        this.usersinroom = usersinroom;
        this.chatlog = chatlog;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setChatroomname(String chatroomname) {
        this.chatroomname = chatroomname;
    }

    public String getChatroomname() {
        return chatroomname;
    }

    public void setUsersinroom(String[] usersinroom) {
        this.usersinroom = usersinroom;
    }

    public String[] getUsersinroom() {
        return usersinroom;
    }

    public void setChatlog(ChatMessage[] chatlog) {
        this.chatlog = chatlog;
    }

    public ChatMessage[] getChatlog() {
        return chatlog;
    }
}
