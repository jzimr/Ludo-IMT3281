package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.SentMessageResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.SentMessageResponse;
import no.ntnu.imt3281.ludo.logic.messages.UserLeftChatRoom;
import no.ntnu.imt3281.ludo.logic.messages.UserSentMessage;
import no.ntnu.imt3281.ludo.server.ChatMessage;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class ChatRoomController implements SentMessageResponseListener {
    @FXML
    private TextArea chatLogText;

    @FXML
    private TextField chatTextInput;

    @FXML
    private Button messageButton;

    @FXML
    private ListView usersList;

    private ClientSocket clientSocket;
    private String chatRoomName;

    private ChatMessage[] chatlog;
    private String[] usersInChatRoom;
    private ObservableList observableList = FXCollections.observableArrayList();
    private List<String> stringList = new ArrayList<>();

    /**
     * Method to pass client socket from LudoController to this
     * and the chat name itself
     */
    public void setup(ClientSocket clientSocket, String chatRoomName) {
        this.clientSocket = clientSocket;
        this.chatRoomName = chatRoomName;

        // add listeners
        clientSocket.addSentMessageResponseListener(this);
        Platform.runLater(() -> addChatLogMessages());
        Platform.runLater(() -> addOnlineUsers());
    }

    /**
     * Called when user presses "Send" button to send message
     *
     * @param event
     */
    @FXML
    void sendMessageButton(ActionEvent event) {
        sendMessage();
    }

    /**
     * When user pressed "Enter" let him send text message
     *
     * @param event
     */
    @FXML
    void onChatKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }

    /**
     * Send a text message to the server
     */
    private void sendMessage() {
        // don't let users send an empty message
        if (chatTextInput.getText().isEmpty()) {
            return;
        }

        // construct the message and send it to the server
        UserSentMessage message = new UserSentMessage("UserSentMessage", clientSocket.getUserId(), chatRoomName,
                chatTextInput.getText());
        clientSocket.sendMessageToServer(message);

        // at last clear the chat input
        chatTextInput.clear();
    }


    private void addChatLogMessages(){
        for(ChatMessage message : chatlog){
            Date date = new Date(message.getTimeSent()*1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            String time = sdf.format(date);
            chatLogText.appendText(time + " - " + message.getdisplayName() + ": " + message.getChatMessage() + "\t\n");
        }
    }

    @FXML
    private void addOnlineUsers(){

        for(int i = 0; i < usersInChatRoom.length; i++){
            stringList.add(usersInChatRoom[i]);
            observableList.add(usersInChatRoom[i]);
        }

        //observableList.setAll(stringList);
        System.out.println(observableList.size());
        usersList.setItems(observableList);
        usersList.setCellFactory((Callback<ListView<String>, ListCell<String>>) listView -> new ListViewCell());

    }

    public void setChatlog(ChatMessage[] chatlog) {
        this.chatlog = chatlog;
    }

    public void setUsersInChatRoom(String[] usersInChatRoom) {
        this.usersInChatRoom = usersInChatRoom;
    }

    @Override
    public boolean equals(String chatName) {
        return this.chatRoomName.equals(chatName);
    }

    /**
     * When the client or another user is sending a message in this particular chat (receiving this from server)
     *
     * @param response an object containing all the necessary info about a chat message sent
     */
    @Override
    public void sentMessageResponseEvent(SentMessageResponse response) {
        // convert time to local time
        LocalDateTime time;
        try {
            time = LocalDateTime.ofEpochSecond(Long.parseLong(response.getTimestamp()), 0, ZoneOffset.ofHours(0));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }

        String timeSent = String.format("%02d:%02d:%02d", time.getHour(), time.getMinute(), time.getSecond());
        String userSent = response.getdisplayname();
        String messageSent = response.getChatmessage();

        Platform.runLater(() -> {
            // display user as red
            chatLogText.appendText(timeSent + " - " + userSent + ": " + messageSent + "\t\n");
        });
    }


    /**
     * Called when the tab of this controller is closed.
     * Here we want to handle stuff like sending message to server.
     */
    public EventHandler<Event> onTabClose = new EventHandler<Event>() {
        @Override
        public void handle(Event arg0) {
            // remove this listener from clientsocket
            clientSocket.removeSentMessageResponseListener(ChatRoomController.this);
            // disconnect user from chat room
            clientSocket.sendMessageToServer(new UserLeftChatRoom("UserLeftChatRoom", clientSocket.getUserId(), chatRoomName));
        }
    };
}
