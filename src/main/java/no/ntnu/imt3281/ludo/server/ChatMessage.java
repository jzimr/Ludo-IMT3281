package no.ntnu.imt3281.ludo.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "chatName",
        "userId",
        "chatMessage",
        "timeSent"
})
public class ChatMessage {
    @JsonProperty("chatName")
    private String chatName;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("chatMessage")
    private String chatMessage;

    @JsonProperty("timeSent")
    private long timeSent;

    public ChatMessage(){}

    public ChatMessage(String chatName, String displayName, String chatMessage, long timeSent){
        this.chatName = chatName;
        this.displayName = displayName;
        this.chatMessage = chatMessage;
        this.timeSent = timeSent;
    }
    @JsonProperty("chatName")
    public String getChatName() {
        return chatName;
    }

    @JsonProperty("displayName")
    public String getdisplayName() {
        return displayName;
    }

    @JsonProperty("chatMessage")
    public String getChatMessage() {
        return chatMessage;
    }

    @JsonProperty("timeSent")
    public long getTimeSent() {
        return timeSent;
    }

    @JsonProperty("chatMessage")
    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    @JsonProperty("chatName")
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    @JsonProperty("timeSent")
    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    @JsonProperty("displayName")
    public void setdisplayName(String displayName) {
        this.displayName = displayName;
    }
}
