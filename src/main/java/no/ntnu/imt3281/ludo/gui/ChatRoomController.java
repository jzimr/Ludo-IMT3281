package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.SentMessageResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.SentMessageResponse;
import no.ntnu.imt3281.ludo.logic.messages.UserLeftChatRoom;
import no.ntnu.imt3281.ludo.logic.messages.UserSentMessage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ChatRoomController implements SentMessageResponseListener {
    @FXML
    private TextArea chatLogText;

    @FXML
    private TextField chatTextInput;

    @FXML
    private Button messageButton;

    private ClientSocket clientSocket;
    private String chatRoomName;

    /**
     * Method to pass client socket from LudoController to this
     * and the chat name itself
     */
    public void setup(ClientSocket clientSocket, String chatRoomName) {
        this.clientSocket = clientSocket;
        this.chatRoomName = chatRoomName;

        // add listeners
        clientSocket.addSentMessageResponseListener(this);
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
            // disconnect user from chat room
            clientSocket.sendMessageToServer(new UserLeftChatRoom("UserLeftChatRoom", clientSocket.getUserId(), chatRoomName));
            // remove this listener from clientsocket
            clientSocket.removeSentMessageResponseListener(ChatRoomController.this);
        }
    };
}
