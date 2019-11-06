package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.client.SessionTokenManager;
import no.ntnu.imt3281.ludo.gui.ServerListeners.LoginResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.RegisterResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.*;

import java.sql.Connection;

public class LoginController implements RegisterResponseListener {

    @FXML
    private TextField serverAddressTextInput;

    @FXML
    private TextField usernameTextInput;

    @FXML
    private TextField passwordTextInput;

    @FXML
    private Text errorMessage;

    @FXML
    public Text successMessage;

    @FXML
    private CheckBox rememberMeBox;

    private ClientSocket clientSocket;

    /**
     * Method to pass client socket from LudoController to this
     */
    public void setClientSocket(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;

        // set listeners
        clientSocket.addRegisterResponseListener(this);
    }

    /**
     * When user presses "Login" button
     *
     * @param event
     */
    @FXML
    void userDoesManualLogin(ActionEvent event) {
        // reset messages
        errorMessage.setText("");
        successMessage.setText("");

        String serverAddress = serverAddressTextInput.getText();
        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();

        // all text fields should be filled out
        if (serverAddress.isEmpty() || password.isEmpty() || username.isEmpty()) {
            errorMessage.setText("Server address, username and password can't be empty!");
            return;
        }

        String ip = serverAddress.split(":")[0];
        String port = serverAddress.split(":")[1];

        // try to connect, if fail don't go any further
        if (!connectToServer(ip, port)) {
            return;
        }

        // send a unique session token + username + password to the server so it knows it's us
        ClientLogin login = new ClientLogin("UserDoesLoginManual", username, password);
        // create a new session token
        String token = SessionTokenManager.generateSessionToken();
        login.setRecipientSessionId(token);

        // if user wants to auto-login next time (i.e. "Remember me" box is checked)
        if(rememberMeBox.isSelected()){
            SessionTokenManager.writeSessionToFile(serverAddress, token);
        }

        // send the message to server
        clientSocket.sendMessageToServer(login);
    }

    /**
     * When user presses "Register" button
     *
     * @param event
     */
    @FXML
    void userDoesRegister(ActionEvent event) {
        // reset messages
        errorMessage.setText("");
        successMessage.setText("");

        String serverAddress = serverAddressTextInput.getText();
        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();

        // all text fields should be filled out
        if (serverAddress.isEmpty() || password.isEmpty() || username.isEmpty()) {
            errorMessage.setText("Server address, username and password can't be empty!");
            return;
        }

        String ip = serverAddress.split(":")[0];
        String port = serverAddress.split(":")[1];

        // try to connect, if fail don't go any further
        if (!connectToServer(ip, port)) {
            return;
        }

        // send a unique session token + username + password to the server so it knows it's us
        ClientRegister register = new ClientRegister("UserDoesRegister", username, password);
        String token = SessionTokenManager.generateSessionToken();
        register.setRecipientSessionId(token);
        clientSocket.sendMessageToServer(register);
    }

    public void setLoginResponseMessage(String message, boolean isSuccess){
        if(isSuccess){
            successMessage.setText("Login success");
        } else {
            errorMessage.setText(message);
        }
    }

    @Override
    public void registerResponseEvent(RegisterResponse response) {
        if(response.isRegisterStatus()){
            successMessage.setText("Register success");
        } else {
            errorMessage.setText(response.getResponse());
        }
    }

    /**
     * Try to connect to server using ip and port which user entered
     *
     * @param ip   the IP address to the server
     * @param port the port to the server
     * @return if connection to server could be made (true), or not (false)
     */
    boolean connectToServer(String ip, String port) {
        // try to connect to the server
        ClientSocket.ConnectionCode responseCode;

        try {
            responseCode = clientSocket.establishConnectionToServer(ip, Integer.parseInt(port));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // wrong port probably, notify client
            errorMessage.setText("The server address is in wrong format. Correct format is <serverIP>:<port>");
            return false;
        }

        // Send specific messages to user in case of success
        switch(responseCode){
            case CONNECTION_OTHER_ERROR:
                errorMessage.setText("Some error happened, could not connect.");
                return false;
            case CONNECTION_REFUSED:
                errorMessage.setText("Could not establish connection to server");
                return false;
            case CONNECTION_SUCCESS:
                return true;
            default:
                return false;
        }
    }
}
