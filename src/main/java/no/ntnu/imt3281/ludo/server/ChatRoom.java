package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;

public class ChatRoom {

    String name;
    boolean gameRoom; //Room that is associated with a game.
    ArrayList<String> connectedUsers = new ArrayList<>(); //ArrayList with user ids.
    ArrayList<String> allowedUsers = new ArrayList<>(); //People allowed into the room.

    ChatRoom(String name) {
        gameRoom = false;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getConnectedUsers() {
        return connectedUsers;
    }

    public void setAllowedUsers(ArrayList<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public ArrayList<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void setGameRoom(boolean gameRoom) {
        this.gameRoom = gameRoom;
    }

    public boolean isGameRoom() {
        return gameRoom;
    }
}
