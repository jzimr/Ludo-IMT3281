package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.client.SessionTokenManager;
import no.ntnu.imt3281.ludo.gui.ServerListeners.LoginResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.RegisterResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.ClientLogin;
import no.ntnu.imt3281.ludo.logic.messages.ClientRegister;
import no.ntnu.imt3281.ludo.logic.messages.LoginResponse;
import no.ntnu.imt3281.ludo.logic.messages.RegisterResponse;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.UUID;

public class LoginController implements LoginResponseListener, RegisterResponseListener {

    @FXML
    private TextField serverAddressTextInput;

    @FXML
    private TextField usernameTextInput;

    @FXML
    private TextField passwordTextInput;

    @FXML
    private Text errorMessage;


    private ClientSocket clientSocket;

    /**
     * Method to pass client socket from LudoController to this
     */
    public void setClientSocket(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * When user presses "Login" button
     *
     * @param event
     */
    @FXML
    void userDoesManualLogin(ActionEvent event) {
        // reset error message
        errorMessage.setText("");

        String serverAddress = serverAddressTextInput.getText();
        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();

        // all text fields should be filled out
        if (serverAddress.isEmpty() || password.isEmpty() || username.isEmpty()) {
            errorMessage.setText("Server address, username and password can't be empty!");
            return;
        }

        // set listener
        clientSocket.addLoginResponseListener(this);

        String ip = serverAddress.split(":")[0];
        String port = serverAddress.split(":")[1];

        // try to connect, if fail don't go any further
        if (!connectToServer(ip, port)) {
            return;
        }

        // send a unique session id + username + password to the server so it knows it's us
        ClientLogin login = new ClientLogin("UserDoesLoginManual", username, password);
        // create a new session token and add it to the message we want to send to server
        createNewSessionToken();
        login.setRecipientSessionId(getSessionToken());

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
        // reset error message
        errorMessage.setText("");

        String serverAddress = serverAddressTextInput.getText();
        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();

        // all text fields should be filled out
        if (serverAddress.isEmpty() || password.isEmpty() || username.isEmpty()) {
            errorMessage.setText("Server address, username and password can't be empty!");
            return;
        }

        // set listener
        clientSocket.addRegisterResponseListener(this);

        String ip = serverAddress.split(":")[0];
        String port = serverAddress.split(":")[1];

        // try to connect, if fail don't go any further
        if (!connectToServer(ip, port)) {
            return;
        }

        // send a unique session id + username + password to the server so it knows it's us
        ClientRegister register = new ClientRegister("UserDoesRegister", username, password);
        createNewSessionToken();
        register.setRecipientSessionId(getSessionToken());
        clientSocket.sendMessageToServer(register);
    }

    @Override
    public void loginResponseEvent(LoginResponse response) {
        if (response.isLoginStatus()) {
            System.out.println("Login success!!");
        } else {
            errorMessage.setText(response.getResponse());
        }
    }

    @Override
    public void registerResponseEvent(RegisterResponse response) {
        if(response.isRegisterStatus()){
            System.out.println("Register success!");
        } else {
            errorMessage.setText(response.getResponse());
        }
    }


    /**
     * Create a new session token for server-client authentication
     */
    void createNewSessionToken() {
        try {
            SessionTokenManager.writeSessionToken("session.data", UUID.randomUUID().toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Get the existing session token for server-client authentication
     * @return the session token
     */
    String getSessionToken() {
        String token;
        // try to get the file
        try {
            token = SessionTokenManager.readSessionToken("session.data");
            return token;
        } catch (NoSuchFileException e) {
            // if no file yet, create a new one
            createNewSessionToken();
            try{
                // then try to get file again
                token = SessionTokenManager.readSessionToken("session.data");
                return token;
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        boolean success;

        try {
            success = clientSocket.establishConnectionToServer(ip, Integer.parseInt(port));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // wrong port probably, notify client
            errorMessage.setText("The server address is in wrong format. Correct format is <serverIP>:<port>");
            return false;
        }

        // no connection to server
        if (!success) {
            errorMessage.setText("Could not establish connection to server");
            return false;
        }

        // all gucci
        return true;
    }

}
