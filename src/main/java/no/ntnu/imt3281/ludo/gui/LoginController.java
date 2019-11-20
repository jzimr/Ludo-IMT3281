package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.client.SessionTokenManager;
import no.ntnu.imt3281.ludo.gui.ServerListeners.LoginResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.RegisterResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.*;

import java.sql.Connection;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginController implements RegisterResponseListener {

    @FXML
    private TextField serverAddressTextInput;

    @FXML
    private TextField usernameTextInput;

    @FXML
    private PasswordField passwordTextInput;

    @FXML
    public Text responseMessage;

    @FXML
    private CheckBox rememberMeBox;

    private ClientSocket clientSocket;
    private ResourceBundle i18Bundle;

    @FXML
    public void initialize(){
        Locale locale = Locale.getDefault();
        i18Bundle = ResourceBundle.getBundle("no.ntnu.imt3281.I18N.Game", locale);
    }

    /**
     * Method to pass client socket from LudoController to this
     */
    public void setClientSocket(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;

        // set listeners
        clientSocket.addRegisterResponseListener(this);
    }

    /**
     * When user presses "Enter" on either username or password field to login
     * @param event
     */
    @FXML
    void onEnterLogin(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER){
            userDoesManualLogin(null);
        }
    }

    /**
     * When user presses "Login" button
     *
     * @param event
     */
    @FXML
    void userDoesManualLogin(ActionEvent event) {
        // reset messages
        responseMessage.setText("");

        String serverAddress = serverAddressTextInput.getText();
        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();

        // all text fields should be filled out
        if (serverAddress.isEmpty() || password.isEmpty() || username.isEmpty()) {
            setResponseMessage(i18Bundle.getString("msg.needBothFields"), true);
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
        responseMessage.setText("");

        String serverAddress = serverAddressTextInput.getText();
        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();

        // all text fields should be filled out
        if (serverAddress.isEmpty() || password.isEmpty() || username.isEmpty()) {
            setResponseMessage(i18Bundle.getString("msg.needBothFields"), true);
            return;
        }

        // all text fields should be filled out
        if (password.length() < 8) {
            setResponseMessage(i18Bundle.getString("msg.passLeastChar"), true);
            return;
        }

        // username can't exceed 24 characters
        if(username.length() >= 24){
            setResponseMessage(i18Bundle.getString("msg.exceedNameChars"), true);
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
            setResponseMessage(i18Bundle.getString("msg.loginSuccess"), false);
        } else {
            setResponseMessage(message, true);
        }
    }

    @Override
    public void registerResponseEvent(RegisterResponse response) {
        if(response.isRegisterStatus()){
            setResponseMessage(i18Bundle.getString("msg.registerSuccess"), false);
        } else {
            setResponseMessage(response.getResponse(), true);
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
            if (!clientSocket.isConnected()) {

            }
            responseCode = clientSocket.establishConnectionToServer(ip, Integer.parseInt(port));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // wrong port probably, notify client
            setResponseMessage(i18Bundle.getString("msg.wrongServerFormat"), true);
            return false;
        }

        // Send specific messages to user in case of success
        switch(responseCode){
            case CONNECTION_OTHER_ERROR:
                setResponseMessage(i18Bundle.getString("msg.someError"), true);
                return false;
            case CONNECTION_REFUSED:
                setResponseMessage(i18Bundle.getString("msg.connectFail"), true);
                return false;
            case CONNECTION_SUCCESS:
                return true;
            default:
                return false;
        }
    }

    private void setResponseMessage(String message, boolean isError){
        Platform.runLater(() -> {
            if(isError){
                responseMessage.setStyle("-fx-fill: red");
            } else {
                responseMessage.setStyle("-fx-fill: green");
            }
            responseMessage.setText(i18Bundle.getString(message));
        });
    }
}
