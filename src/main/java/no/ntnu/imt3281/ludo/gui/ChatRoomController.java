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
import javafx.scene.text.Font;
import javafx.util.Callback;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.ChatJoinNewUserResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.SentMessageResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.UserLeftChatRoomResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.*;
import no.ntnu.imt3281.ludo.server.ChatMessage;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class ChatRoomController implements SentMessageResponseListener, ChatJoinNewUserResponseListener,
        UserLeftChatRoomResponseListener {
    @FXML
    private TextArea chatLogText;

    @FXML
    private TextField chatTextInput;

    @FXML
    private Button messageButton;

    @FXML
    private ListView<String> userList;

    private ClientSocket clientSocket;
    private String chatRoomName;

    ObservableList<String> usersInChatRoom;

    /**
     * Method to pass client socket from LudoController to this
     * and the chat name itself
     */
    public void setup(ClientSocket clientSocket, String chatRoomName, final ChatMessage[] chatLog, final String[] usersInChatRoom) {
        this.clientSocket = clientSocket;
        this.chatRoomName = chatRoomName;

        // add listeners
        clientSocket.addSentMessageResponseListener(this);
        clientSocket.addChatJoinNewUserResponseListener(this);
        clientSocket.addUserLeftChatRoomResponseListener(this);

        // add chat history to the chat
        for(int i = 0; i < chatLog.length; i++) {
            ChatMessage message = chatLog[i];

            Date date = new Date(message.getTimeSent() * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            String time = sdf.format(date);

            Platform.runLater(() -> {
                chatLogText.appendText(time + " - " + message.getdisplayName() + ": " + message.getChatMessage() + "\t\n");
            });
        }
        // add all online users in a list
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // change the font of the text inside the cells
                userList.setCellFactory(cell -> new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }
                        else if(item != null) {
                            setText(item);
                            setFont(Font.font(16));
                        }
                    }
                });

                ChatRoomController.this.usersInChatRoom = FXCollections.observableArrayList(usersInChatRoom);
                userList.setItems(ChatRoomController.this.usersInChatRoom);
            }
        });
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

    @Override
    public boolean equalsChatRoomId(String chatName) {
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
        System.out.println("yes");

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
     * Message when we or another user joined the chat room (used for keeping track of people in chat)
     * @param response
     */
    @Override
    public void chatJoinNewUserResponseEvent(ChatJoinNewUserResponse response) {
        Platform.runLater(() -> {
            usersInChatRoom.add(response.getDisplayname());
            //userList.setItems(usersInChatRoom);
        });
    }

    /**
     * Message when another user left the chat room (used for keeping track of people in chat)
     * @param response
     */
    @Override
    public void userLeftChatRoomResponseEvent(UserLeftChatRoomResponse response) {
        Platform.runLater(() ->  {
            usersInChatRoom.remove(response.getDisplayname());
            //userList.setItems(usersInChatRoom);
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
            clientSocket.removeChatJoinNewUserResponseListener(ChatRoomController.this);
            clientSocket.removeUserLeftChatRoomResponseListener(ChatRoomController.this);

            // disconnect user from chat room
            clientSocket.sendMessageToServer(new UserLeftChatRoom("UserLeftChatRoom", clientSocket.getUserId(), chatRoomName));
        }
    };
}
