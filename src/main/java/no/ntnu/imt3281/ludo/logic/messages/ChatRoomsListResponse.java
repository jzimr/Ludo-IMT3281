package no.ntnu.imt3281.ludo.logic.messages;

public class ChatRoomsListResponse extends Message {
    String[] ChatRoom;

    public ChatRoomsListResponse(String action){super(action);}

    public void setChatRoom(String[] chatRoom) {
        ChatRoom = chatRoom;
    }

    public String[] getChatRoom() {
        return ChatRoom;
    }

}
