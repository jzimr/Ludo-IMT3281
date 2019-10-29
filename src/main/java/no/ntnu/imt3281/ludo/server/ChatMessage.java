package no.ntnu.imt3281.ludo.server;

public class ChatMessage {
    private final String chatName;
    private final int userId;
    private final String chatMessage;
    private final long timeSent;

    public ChatMessage(String chatName, int userId, String chatMessage, long timeSent){
        this.chatName = chatName;
        this.userId = userId;
        this.chatMessage = chatMessage;
        this.timeSent = timeSent;
    }

    public String getChatName() {
        return chatName;
    }

    public int getUserId() {
        return userId;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public long getTimeSent() {
        return timeSent;
    }
}
