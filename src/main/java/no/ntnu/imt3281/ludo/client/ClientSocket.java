package no.ntnu.imt3281.ludo.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import no.ntnu.imt3281.ludo.gui.ServerListeners.*;
import no.ntnu.imt3281.ludo.logic.messages.*;
import no.ntnu.imt3281.ludo.server.ChatMessage;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class ClientSocket {
    private static final int DEFAULT_PORT = 4567;
    private Socket connection = null;
    private boolean connected = false;
    private String userId = null;
    private String displayName = null;
    protected BufferedWriter bw;
    protected BufferedReader br;

    /**
     * Types of results we can get when user tries to connect to a server
     */
    public enum ConnectionCode {
        CONNECTION_SUCCESS,
        CONNECTION_REFUSED,
        CONNECTION_OTHER_ERROR
    }

    private LoginResponseListener loginResponseListener = null;
    private RegisterResponseListener registerResponseListener = null;
    private ChatJoinResponseListener chatJoinResponseListener = null;
    private ArrayBlockingQueue<SentMessageResponseListener> sentMessageResponseListeners = new ArrayBlockingQueue<>(100);    // max of 100 chats at once
    private ChatRoomsListResponseListener chatRoomsListResponseListener = null;
    private UsersListResponseListener usersListResponseListener = null;
    private CreateGameResponseListener createGameResponseListener = null;
    private SendGameInvitationsResponseListener sendGameInvitationsResponseListener = null;
    private UserJoinedGameResponseListener userJoinedGameResponseListener = null;
    private ArrayBlockingQueue<UserLeftGameResponseListener> userLeftGameResponseListeners = new ArrayBlockingQueue<>(100); // max of 100 listeners at once
    private ArrayBlockingQueue<GameHasStartedResponseListener> gameHasStartedResponseListeners = new ArrayBlockingQueue<>(100); // max of 100 listeners at once
    private ArrayBlockingQueue<DiceThrowResponseListener> diceThrowResponseListeners = new ArrayBlockingQueue<>(100); // max of 100 listeners at once
    private ArrayBlockingQueue<PieceMovedResponseListener> pieceMovedResponseListeners = new ArrayBlockingQueue<>(100); // max of 100 listeners at once
    private ArrayBlockingQueue<ChatJoinNewUserResponseListener> chatJoinNewUserResponseListeners = new ArrayBlockingQueue<>(100);
    private ArrayBlockingQueue<UserLeftChatRoomResponseListener> userLeftChatRoomResponseListeners = new ArrayBlockingQueue<>(100);
    private UserWantToViewProfileResponseListener userWantToViewProfileResponseListener = null;
    private UserWantToEditProfileResponseListener userWantToEditProfileResponseListener = null;

    /**
     * Create a connection from client to server
     *
     * @param serverIP the IP address of the server
     * @param port     the port of the server, do -1 if default
     */
    public ConnectionCode establishConnectionToServer(String serverIP, int port) {
        // try to connect
        try {
            if (port == -1) {
                port = DEFAULT_PORT;
            }


            if (connection == null || !connection.isConnected()) { //First connection

                connection = new Socket(serverIP, port);
                bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            } else if (connection.isConnected() && //If client is connected
                    (connection.getPort() != port //And either the port or the ip has changed.
                            || !connection.getInetAddress().toString().substring(1).contentEquals(serverIP))) { //Disconnect old socket
                                                                                                                // And connect to the new.
                connection.close();
                connection = new Socket(serverIP, port);
                bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            }
            //connection = new Socket(serverIP, port);
            //bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            //br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            listenToServer();

            connected = true;
            return ConnectionCode.CONNECTION_SUCCESS;
        } catch (ConnectException e) {
            System.out.println("Could not create connection to server");
            return ConnectionCode.CONNECTION_REFUSED;
        } catch (IOException e) {
            e.printStackTrace();
            // todo throw message to user
            return ConnectionCode.CONNECTION_OTHER_ERROR;
        }
    }

    /**
     * Close the connection to the server (if connected)
     */
    public boolean closeConnectionToServer() {
        try {
            connection.close();
            connected = false;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send an event message to the server
     *
     * @param message the message type to send to server
     */
    public void sendMessageToServer(Message message) {
        // todo change
        if (!connected)
            return;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(message);  // json message to send

            System.out.println("Sending message to server: " + jsonMessage);

            bw.write(jsonMessage);          // write to outputstream
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // todo send message to user
        }
    }

    /**
     * Create own thread that listens to the server sending the client messages
     */
    private void listenToServer() {
        Thread t = new Thread() {
            public void run() {
                while (true) {
                    if (connected) {
                        try {
                            final String inMessage = br.readLine();
                            // todo trenger denne en queue?
                            handleMessagesFromServer(inMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // if no connection to server, let's sleep for 50 milliseconds
                        // then try again
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            System.out.println("Server timeout: " + e.getMessage());
                        }
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
     * Gets messages (json) from the listener and deserializes them into the correct objects of type "Message"
     *
     * @param jsonMessage json message received from server
     */
    private void handleMessagesFromServer(String jsonMessage) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonMessage);
            String action = jsonNode.get("action").asText();

            if (!action.equals("Ping"))
                System.out.println("Got message from server: " + jsonMessage);

            switch (action) {
                case "Ping":    // we don't want to do anything here.
                    return;
                case "LoginResponse":
                    LoginResponse message1 = new LoginResponse(action, jsonNode.get("response").asText(),
                            jsonNode.get("loginStatus").asBoolean(), jsonNode.get("userid").asText(), jsonNode.get("displayname").asText());
                    userId = message1.getUserid();                          // get the userId from server on login so we can send messages back to server when needed
                    displayName = message1.getDisplayname();                // get the displayname of this client so we can identify him in the ludogame
                    if (userId == null)
                        closeConnectionToServer();           // if we could not get userId from server, something went wrong, so we close connection
                    loginResponseListener.loginResponseEvent(message1);     // send the event to the desired listener
                    break;
                case "RegisterResponse":
                    RegisterResponse message2 = new RegisterResponse(action, jsonNode.get("response").asText(),
                            jsonNode.get("registerStatus").asBoolean());
                    registerResponseListener.registerResponseEvent(message2);
                    break;
                case "ChatJoinResponse":
                    //Get and create array of usersinroom
                    ArrayList<String> usersinroom = objectMapper.convertValue(jsonNode.get("usersinroom"), ArrayList.class);
                    String[] usersInRoomArr = new String[usersinroom.size()];
                    for(int i = 0; i < usersinroom.size(); i++) {
                        usersInRoomArr[i] = usersinroom.get(i);
                    }

                    //Get and create array of chat messages.
                    ArrayList<ChatMessage> chatlog = objectMapper.convertValue(jsonNode.get("chatlog"), new TypeReference <ArrayList<ChatMessage>>(){});
                    ChatMessage[] chatlogArr = new ChatMessage[chatlog.size()];
                    for(int i = 0; i < chatlog.size(); i++) {
                        chatlogArr[i] = chatlog.get(i);
                    }

                    ChatJoinResponse message3 = new ChatJoinResponse(action, jsonNode.get("status").asBoolean(),
                            jsonNode.get("response").asText(), jsonNode.get("chatroomname").asText(), usersInRoomArr, chatlogArr);
                    chatJoinResponseListener.chatJoinResponseEvent(message3);
                    break;
                case "SentMessageResponse":
                    SentMessageResponse message4 = new SentMessageResponse(action, jsonNode.get("displayname").asText(),
                            jsonNode.get("chatroomname").asText(), jsonNode.get("chatmessage").asText(),
                            jsonNode.get("timestamp").asText());

                    // send the message to the correct listener
                    SentMessageResponseListener listener = sentMessageResponseListeners.stream()
                            .filter(l -> l != null && l.equalsChatRoomId(message4.getChatroomname())).findFirst().orElse(null);
                    if (listener != null) listener.sentMessageResponseEvent(message4);
                    break;
                case "ChatRoomsListResponse":
                    // we get the String[] list from the jackson node
                    ArrayNode chatRoomsNode = (ArrayNode) jsonNode.get("chatRoom");
                    String[] chatRooms = new String[chatRoomsNode.size()];
                    for (int i = 0; i < chatRoomsNode.size(); i++) {
                        chatRooms[i] = chatRoomsNode.get(i).asText();
                    }

                    ChatRoomsListResponse message5 = new ChatRoomsListResponse(action, chatRooms);
                    chatRoomsListResponseListener.chatRoomsListResponseEvent(message5);
                    break;
                case "UsersListResponse":
                    // we get the String[] list from the jackson node
                    ArrayNode userListNode = (ArrayNode) jsonNode.get("displaynames");
                    String[] userList = new String[userListNode.size()];
                    for (int i = 0; i < userListNode.size(); i++) {
                        userList[i] = userListNode.get(i).asText();
                    }

                    UsersListResponse message6 = new UsersListResponse(action, userList);
                    usersListResponseListener.usersListResponseEvent(message6);
                    break;
                case "CreateGameResponse":
                    CreateGameResponse message7 = new CreateGameResponse(action, jsonNode.get("gameid").asText(),
                            jsonNode.get("joinstatus").asBoolean(), jsonNode.get("response").asText());
                    createGameResponseListener.createGameResponseEvent(message7);
                    break;
                case "SendGameInvitationsResponse":
                    SendGameInvitationsResponse message8 = new SendGameInvitationsResponse(action,
                            jsonNode.get("gameid").asText(), jsonNode.get("hostdisplayname").asText());
                    sendGameInvitationsResponseListener.sendGameInvitationsResponseEvent(message8);
                    break;
                case "UserJoinedGameResponse":
                    // we get the String[] list from the jackson node
                    ArrayNode lobbyListNode = (ArrayNode) jsonNode.get("playersinlobby");
                    String[] lobbyList = new String[lobbyListNode.size()];
                    for (int i = 0; i < lobbyListNode.size(); i++) {
                        lobbyList[i] = lobbyListNode.get(i).asText();
                    }

                    UserJoinedGameResponse message9 = new UserJoinedGameResponse(action, jsonNode.get("userid").asText(),
                            jsonNode.get("gameid").asText(), lobbyList);
                    userJoinedGameResponseListener.userJoinedGameResponseEvent(message9);
                    break;
                case "UserLeftGameResponse":
                    UserLeftGameResponse message10 = new UserLeftGameResponse(action, jsonNode.get("displayname").asText(),
                            jsonNode.get("gameid").asText());

                    // send the message to the correct listener
                    UserLeftGameResponseListener listener2 = userLeftGameResponseListeners.stream()
                            .filter(l -> l != null && l.equalsGameId(message10.getGameid())).findFirst().orElse(null);
                    if (listener2 != null) listener2.userLeftGameResponseEvent(message10);
                    break;
                case "GameHasStartedResponse":
                    GameHasStartedResponse message11 = new GameHasStartedResponse(action, jsonNode.get("gameid").asText());

                    // send the message to the correct listener
                    GameHasStartedResponseListener listener3 = gameHasStartedResponseListeners.stream()
                            .filter(l -> l != null && l.equalsGameId(message11.getGameid())).findFirst().orElse(null);
                    if (listener3 != null) listener3.gameHasStartedResponseEvent(message11);
                    break;
                case "PlayerStateChangeResponse":
                    // todo
                    break;
                case "DiceThrowResponse":
                    DiceThrowResponse message12 = new DiceThrowResponse(action, jsonNode.get("gameid").asText(),
                            jsonNode.get("dicerolled").asInt());

                    // send the message to the correct listener
                    DiceThrowResponseListener listener4 = diceThrowResponseListeners.stream()
                            .filter(l -> l != null && l.equalsGameId(message12.getGameid())).findFirst().orElse(null);
                    if (listener4 != null) listener4.diceThrowResponseEvent(message12);
                    break;
                case "PieceMovedResponse":
                    PieceMovedResponse message13 = new PieceMovedResponse(action, jsonNode.get("gameid").asText(),
                            jsonNode.get("playerid").asInt(), jsonNode.get("piecemoved").asInt(), jsonNode.get("movedfrom").asInt(),
                            jsonNode.get("movedto").asInt());

                    // send the message to the correct listener
                    PieceMovedResponseListener listener5 = pieceMovedResponseListeners.stream()
                            .filter(l -> l != null && l.equalsGameId(message13.getGameid())).findFirst().orElse(null);
                    if (listener5 != null) listener5.pieceMovedResponseEvent(message13);
                    break;
                case "ChatJoinNewUserResponse":
                    ChatJoinNewUserResponse message14 = new ChatJoinNewUserResponse(action, jsonNode.get("displayname").asText(),
                            jsonNode.get("chatroomname").asText());

                    // send the message to the correct listener
                    ChatJoinNewUserResponseListener listener6 = chatJoinNewUserResponseListeners.stream()
                            .filter(l -> l != null && l.equalsChatRoomId(message14.getChatroomname())).findFirst().orElse(null);
                    if (listener6 != null) listener6.chatJoinNewUserResponseEvent(message14);
                    break;
                case "UserLeftChatRoomResponse":
                    UserLeftChatRoomResponse message15 = new UserLeftChatRoomResponse(action, jsonNode.get("chatroomname").asText(),
                            jsonNode.get("displayname").asText());

                    // send the message to the correct listener
                    UserLeftChatRoomResponseListener listener7 = userLeftChatRoomResponseListeners.stream()
                            .filter(l -> l != null && l.equalsChatRoomId(message15.getChatroomname())).findFirst().orElse(null);
                    if (listener7 != null) listener7.userLeftChatRoomResponseEvent(message15);
                    break;
                case "UserWantToViewProfileResponse":
                    UserWantToViewProfileResponse message16 = new UserWantToViewProfileResponse(action, jsonNode.get("userId").asText(),
                            jsonNode.get("displayName").asText(), jsonNode.get("imageString").binaryValue(), jsonNode.get("gamesPlayed").asInt(),
                            jsonNode.get("gamesWon").asInt());

                    // send message to listener
                    userWantToViewProfileResponseListener.userWantToViewProfileResponseEvent(message16);
                    break;
                case "UserWantToEditProfileResponse":
                    UserWantToEditProfileResponse message17 = new UserWantToEditProfileResponse(action, jsonNode.get("changed").asBoolean(),
                            jsonNode.get("response").asText(), jsonNode.get("displayname").asText());

                    // update our displayname in case of name change
                    displayName = message17.getDisplayname();

                    userWantToEditProfileResponseListener.userWantToEditProfileResponseEvent(message17);
                    break;
                default:
                    System.out.println("Json not recognized: " + jsonMessage);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the ID of the account which the client has logged on with.
     *
     * @return the String of user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the displayname of the client which he uses to be identified by other players
     *
     * @return the display name of the client
     */
    public String getDisplayName() {
        return displayName;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Here we register all listeners for all the server messages we get, so we can delegate
     * them on to the GUI part of the application
     */

    /**
     * @param listener
     */
    public void addLoginResponseListener(LoginResponseListener listener) {
        loginResponseListener = listener;
    }

    public void addRegisterResponseListener(RegisterResponseListener listener) {
        registerResponseListener = listener;
    }

    public void addChatJoinResponseListener(ChatJoinResponseListener listener) {
        chatJoinResponseListener = listener;
    }

    public void addSentMessageResponseListener(SentMessageResponseListener listener) {
        sentMessageResponseListeners.add(listener);
    }

    public void removeSentMessageResponseListener(SentMessageResponseListener listener) {
        sentMessageResponseListeners.remove(listener);
    }

    public void addChatRoomsListResponseListener(ChatRoomsListResponseListener listener) {
        chatRoomsListResponseListener = listener;
    }

    public void addUsersListResponseListener(UsersListResponseListener listener) {
        usersListResponseListener = listener;
    }

    public void addCreateGameResponseListener(CreateGameResponseListener listener) {
        createGameResponseListener = listener;
    }

    public void addSendGameInvitationsResponseListener(SendGameInvitationsResponseListener listener) {
        sendGameInvitationsResponseListener = listener;
    }

    public void addUserJoinedGameResponseListener(UserJoinedGameResponseListener listener) {
        userJoinedGameResponseListener = listener;
    }

    public void addUserLeftGameResponseListener(UserLeftGameResponseListener listener) {
        userLeftGameResponseListeners.add(listener);
    }

    public void removeUserLeftGameResponseListener(UserLeftGameResponseListener listener) {
        userLeftGameResponseListeners.remove(listener);
    }

    public void addGameHasStartedResponseListener(GameHasStartedResponseListener listener) {
        gameHasStartedResponseListeners.add(listener);
    }

    public void removeGameHasStartedResponseListener(GameHasStartedResponseListener listener) {
        gameHasStartedResponseListeners.remove(listener);
    }

    public void addDiceThrowResponseListener(DiceThrowResponseListener listener) {
        diceThrowResponseListeners.add(listener);
    }

    public void removeDiceThrowResponseListener(DiceThrowResponseListener listener) {
        diceThrowResponseListeners.remove(listener);
    }

    public void addPieceMovedResponseListener(PieceMovedResponseListener listener){
        pieceMovedResponseListeners.add(listener);
    }

    public void removePieceMovedListener(PieceMovedResponseListener listener){
        pieceMovedResponseListeners.remove(listener);
    }

    public void addChatJoinNewUserResponseListener(ChatJoinNewUserResponseListener listener){
        chatJoinNewUserResponseListeners.add(listener);
    }

    public void removeChatJoinNewUserResponseListener(ChatJoinNewUserResponseListener listener){
        chatJoinNewUserResponseListeners.remove(listener);
    }

    public void addUserLeftChatRoomResponseListener(UserLeftChatRoomResponseListener listener){
        userLeftChatRoomResponseListeners.add(listener);
    }

    public void removeUserLeftChatRoomResponseListener(UserLeftChatRoomResponseListener listener){
        userLeftChatRoomResponseListeners.remove(listener);
    }

    public void addUserWantToViewProfileResponseListener(UserWantToViewProfileResponseListener listener){
        userWantToViewProfileResponseListener = listener;
    }

    public void addUserWantToEditProfileResponseListener(UserWantToEditProfileResponseListener listener){
        userWantToEditProfileResponseListener = listener;
    }
}
