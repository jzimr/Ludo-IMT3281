package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.logic.messages.ClientLogin;

import java.util.UUID;

public class LoginController {

    @FXML
    private TextField serverAddressTextInput;

    @FXML
    private TextField usernameTextInput;

    @FXML
    private TextField passwordTextInput;


    private ClientSocket clientSocket;

    /**
     *
     */
    public void setClientSocket(ClientSocket clientSocket){
        this.clientSocket = clientSocket;
    }

    /**
     * When user presses "Login" button
     * @param event
     */
    @FXML
    void userDoesLogin(ActionEvent event) {
        String serverAddress = serverAddressTextInput.getText();
        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();
        // all text fields should be filled out
        if(serverAddress.isEmpty()
        || password.isEmpty()
        || username.isEmpty()){
            return; // todo show message to user
        }

        String ip = serverAddress.split(":")[0];
        String port = serverAddress.split(":")[1];

        // try to connect to the server
        boolean success;
        try{
            success = clientSocket.establishConnectionToServer(ip, Integer.parseInt(port));
        } catch(NumberFormatException e){
            e.printStackTrace();
            return; // todo show message to user about wrong port (probs)
        }

        // send a unique session id + username + password to the server so it knows it's us
        ClientLogin login = new ClientLogin("UserDoesLoginManual", username, password);
        login.setRecipientSessionId(UUID.randomUUID().toString());
        clientSocket.sendMessageToServer(login);


    }

    /**
     * When user presses "New User?" button
     * @param event
     */
    @FXML
    void userDoesRegister(ActionEvent event) {

    }

}
