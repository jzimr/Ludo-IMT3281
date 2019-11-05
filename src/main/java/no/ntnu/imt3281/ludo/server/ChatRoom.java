package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;

public class ChatRoom {

    String name;
    ArrayList<String> connectedUsers = new ArrayList<>(); //ArrayList with user ids.

    ChatRoom(String name) {
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


}
