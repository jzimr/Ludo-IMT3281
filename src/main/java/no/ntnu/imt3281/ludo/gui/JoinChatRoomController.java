package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.logic.messages.UserJoinChat;

public class JoinChatRoomController {

    @FXML
    private Button joinChatRoomButton;

    @FXML
    private TextField chatNameTextInput;

    @FXML
    private Text responseMessage;

    private ClientSocket clientSocket;

    /**
     * Method to pass client socket from LudoController to this
     */
    public void setClientSocket(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * When user pressed "Join" button
     * @param event
     */
    @FXML
    void joinChatRoom(ActionEvent event) {
        String userId = clientSocket.getUserId();
        String chatName = chatNameTextInput.getText();

        // textbox cannot be empty
        if(chatName.isEmpty()){
            responseMessage.setText("Chat room cannot be empty");
            return;
        }

        // send message to server that we want to join the particular chat channel (if exists).
        // If not exists the chat channel will be created.
        clientSocket.sendMessageToServer(new UserJoinChat("UserJoinChat", chatName, userId));
    }

    /**
     * Set the error message
     * @param message the message to display to user
     * @param isError true if message should be displayed as error, or false if should be displayed as success
     */
    public void setResponseMessage(String message, boolean isError){
        // set colour on text depending if success or error message
        Platform.runLater(() -> {
            if(isError){
                responseMessage.setStyle("-fx-fill: red");
            } else {
                responseMessage.setStyle("-fx-fill: green");
            }
            responseMessage.setText(message);
        });
    }
}
