package no.ntnu.imt3281.ludo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.util.Callback;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.ChatRoomsListResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.ChatRoomsListResponse;

public class ChatRoomsListController implements ChatRoomsListResponseListener {

    @FXML
    private ListView<String> chatRoomListView;

    @FXML
    public void initialize(){
        // change the font of the text inside the cells
        chatRoomListView.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item);
                    setFont(Font.font(16));
                }
            }
        });
    }

    /**
     * Method to pass client socket from LudoController to setup listeners
     */
    public void setup(ClientSocket clientSocket) {

        // add listener
        clientSocket.addChatRoomsListResponseListener(this);
    }


    /**
     * When we get the rooms list message from the server
     * @param response the message object we get from server
     */
    @Override
    public void chatRoomsListResponseEvent(ChatRoomsListResponse response) {
        ObservableList<String> items = FXCollections.observableArrayList(response.getChatRoom());
        chatRoomListView.setItems(items);
    }
}
