package no.ntnu.imt3281.ludo.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import no.ntnu.imt3281.ludo.gui.ServerListeners.*;
import no.ntnu.imt3281.ludo.logic.messages.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class ClientSocket {
    private static final int DEFAULT_PORT = 4567;
    private Socket connection = null;
    private boolean connected = false;
    private String userId = null;
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

    LoginResponseListener loginResponseListener = null;
    RegisterResponseListener registerResponseListener = null;
    ChatJoinResponseListener chatJoinResponseListener = null;
    SentMessageResponseListener sentMessageResponseListener = null;
    ChatRoomsListResponseListener chatRoomsListResponseListener = null;

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
            connection = new Socket(serverIP, port);
            bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

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

            if(!action.equals("Ping"))
                System.out.println("Got message from server: " + jsonMessage);

            switch (action) {
                case "Ping":    // we don't want to do anything here.
                    return;
                case "LoginResponse":
                    LoginResponse message1 = new LoginResponse(action, jsonNode.get("response").asText(),
                            jsonNode.get("loginStatus").asBoolean(), jsonNode.get("userid").asText());
                    userId = message1.getUserid();                          // get the userId from server on login so we can send messages back to server when needed
                    if(userId == null) closeConnectionToServer();           // if we could not get userId from server, something went wrong, so we close connection
                    loginResponseListener.loginResponseEvent(message1);     // send the event to the desired listener
                    break;
                case "RegisterResponse":
                    RegisterResponse message2 = new RegisterResponse(action, jsonNode.get("response").asText(),
                            jsonNode.get("registerStatus").asBoolean());
                    registerResponseListener.registerResponseEvent(message2);
                    break;
                case "ChatJoinResponse":
                    ChatJoinResponse message3 = new ChatJoinResponse(action, jsonNode.get("status").asBoolean(),
                            jsonNode.get("response").asText(), jsonNode.get("chatroomname").asText());
                    chatJoinResponseListener.chatJoinResponseEvent(message3);
                    break;
                case "SentMessageResponse":
                    SentMessageResponse message4 = new SentMessageResponse(action, jsonNode.get("displayname").asText(),
                            jsonNode.get("chatroomname").asText(), jsonNode.get("chatmessage").asText(),
                            jsonNode.get("timestamp").asText());
                    sentMessageResponseListener.sentMessageResponseEvent(message4);
                    break;
                case "ChatRoomsListResponse":
                    // we get the String[] list from the jackson node
                    ArrayNode chatRoomsNode = (ArrayNode)jsonNode.get("chatRoom");
                    String[] chatRooms = new String[chatRoomsNode.size()];
                    for(int i = 0; i < chatRoomsNode.size(); i++){
                        chatRooms[i] = chatRoomsNode.get(i).asText();
                    }

                    ChatRoomsListResponse message5 = new ChatRoomsListResponse(action, chatRooms);
                    chatRoomsListResponseListener.chatRoomsListResponseEvent(message5);
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

    public void addSentMessageResponseListener(SentMessageResponseListener listener){
        sentMessageResponseListener = listener;
    }

    public void addChatRoomsListResponseListener(ChatRoomsListResponseListener listener){
        chatRoomsListResponseListener = listener;
    }
}
