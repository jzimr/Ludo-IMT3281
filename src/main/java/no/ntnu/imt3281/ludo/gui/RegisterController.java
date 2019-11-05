package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import no.ntnu.imt3281.ludo.client.ClientSocket;

public class RegisterController {

    @FXML
    private Button registerButton;

    @FXML
    private TextField passwordTextInput;

    @FXML
    private TextField password2TextInput;

    @FXML
    private TextField usernameTextInput;

    @FXML
    private Text usernameExistsText;

    @FXML
    private Button cancelButton;

    private ClientSocket clientSocket;

    /**
     * Method to pass client socket from LudoController to this
     */
    public void setClientSocket(ClientSocket clientSocket){
        this.clientSocket = clientSocket;
    }

    @FXML
    public void initialize(){

    }

    /**
     * When user presses "Cancel" button
     * @param event
     */
    @FXML
    void userCancelsRegister(ActionEvent event) {

    }

    /**
     * When user presses "Register" button
     * @param event
     */
    @FXML
    void userDoesRegister(ActionEvent event) {

    }

}
