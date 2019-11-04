package no.ntnu.imt3281.ludo.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.messages.Message;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private static final int DEFAULT_PORT = 4567;
    private Socket connection = null;
    private boolean connected = false;
    protected BufferedWriter bw;
    protected BufferedReader br;

    //ArrayBlockingQueue<Message> serverMessages = new ArrayBlockingQueue<>(100);

    /**
     * Create a connection from client to server
     *
     * @param serverIP the IP address of the server
     * @param port the port of the server, do -1 if default
     */
    public boolean establishConnectionToServer(String serverIP, int port) {
        // try to connect
        try {
            if(port == -1){
                port = DEFAULT_PORT;
            }
            connection = new Socket(serverIP, port);
            bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            listenToServer();

            connected = true;
            return true;

            // todo test connection by sending message?
        } catch (IOException e) {
            e.printStackTrace();
            // todo throw message to user
            return false;
        }
    }

    /**
     * Close the connection to the server (if connected)
     */
    public boolean closeConnectionToServer() {
        if (connected) {
            try {
                connection.close();
                connected = false;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Send an event message to the server
     *
     * @param message the message type to send to server
     */
    public void sendMessageToServer(Message message) {
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
                            System.out.println("Got message from server: " + inMessage);
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
     * @param jsonMessage json message received from server
     */
    private void handleMessagesFromServer(String jsonMessage) {
        ObjectMapper objectMapper = new ObjectMapper();
        Message message = null;

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonMessage);
            String action = jsonNode.get("action").asText();

            switch(action){
                // todo LoginResponse
                // todo RegisterRResponse
            }
            /*
            switch (action) {
                case "UserDoesLoginManual":
                    message = new ClientLogin(jsonNode.get("action").asText(), jsonNode.get("username").asText(), jsonNode.get("password").asText());
                default:
                    System.out.println("Json not recognized: " + jsonMessage);
            }
             */

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLoginResponse(){

    }

    private void handleRegisterResponse(){

    }




}
