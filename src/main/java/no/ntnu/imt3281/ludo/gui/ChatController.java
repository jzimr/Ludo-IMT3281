package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.ChatJoinResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.ChatJoinResponse;

public class ChatController {
    @FXML
    private TextArea chatLogText;

    @FXML
    private TextField chatTextInput;

    @FXML
    private Button messageButton;

    private ClientSocket clientSocket;

    /**
     * Method to pass client socket from LudoController to this
     */
    public void setClientSocket(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Called when user presses "Send" button to send message
     * @param event
     */
    @FXML
    void sendMessageButton(ActionEvent event) {
        chatTextInput.clear();

        // todo
    }




    /**
     * Called when the tab of this controller is closed.
     * Here we want to handle stuff like sending message to server.
     */
    public EventHandler<Event> onTabClose = new EventHandler<Event>()
    {
        @Override
        public void handle(Event arg0){
            // todo when user closes tab
            // todo send message to server that we left
        }
    };
}
