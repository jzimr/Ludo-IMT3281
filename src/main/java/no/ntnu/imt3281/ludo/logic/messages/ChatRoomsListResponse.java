package no.ntnu.imt3281.ludo.logic.messages;

public class ChatRoomsListResponse extends Message {
    String[] ChatRoom;

    public ChatRoomsListResponse(String action){super(action);}

    public ChatRoomsListResponse(String action, String[] chatRooms){
        super(action);
        this.ChatRoom = chatRooms;
    }

    public void setChatRoom(String[] chatRoom) {
        ChatRoom = chatRoom;
    }

    public String[] getChatRoom() {
        return ChatRoom;
    }

}
