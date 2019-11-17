package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.client.ImageManager;
import no.ntnu.imt3281.ludo.gui.ServerListeners.UserWantToEditProfileResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.UserWantToEditProfile;
import no.ntnu.imt3281.ludo.logic.messages.UserWantToEditProfileResponse;
import no.ntnu.imt3281.ludo.logic.messages.UserWantToViewProfileResponse;

import java.io.File;

public class ViewProfileController implements UserWantToEditProfileResponseListener {

    @FXML
    private ImageView avatarImage;

    @FXML
    private Label displayNameText;

    @FXML
    private Button editDisplayName;

    @FXML
    private Label userIdText;

    @FXML
    private Button editAvatar;

    @FXML
    private Button editPassword;

    @FXML
    private Text playedText;

    @FXML
    private Text wonText;

    private ClientSocket clientSocket;

    // create a new dialog that contains our name in the field already
    TextInputDialog displayNameDialog;
    TextInputDialog passwordDialog;

    // we save the image in form of bytes as well
    byte[] imageString;


    public void setup(ClientSocket clientSocket, final UserWantToViewProfileResponse response, boolean isOurProfile) {
        this.clientSocket = clientSocket;

        // register listeners
        clientSocket.addUserWantToEditProfileResponseListener(this);

        // load all the data into the GUI
        Platform.runLater(() -> {
            displayNameDialog = new TextInputDialog(clientSocket.getDisplayName());
            passwordDialog = new TextInputDialog();
            // if user has set own image, else we use default
            if(response.getImageString() != null){
                Image image = ImageManager.convertBytesToImage(response.getImageString());
                imageString = response.getImageString();
                // check that it decoded without problems
                if(image != null){
                    avatarImage.setImage(image);
                }
            }

            // set all other fields
            displayNameText.setText(response.getDisplayName());
            userIdText.setText(response.getUserId());
            playedText.setText(String.valueOf(response.getGamesPlayed()));
            wonText.setText(String.valueOf(response.getGamesWon()));

            // if we view our own profile, we want to be able to edit it
            if(isOurProfile){
                // make buttons visible
                editDisplayName.setDisable(false);
                editDisplayName.setVisible(true);
                editPassword.setDisable(false);
                editPassword.setVisible(true);
                editAvatar.setDisable(false);
                editAvatar.setVisible(true);
            }
        });
    }

    @FXML
    void editAvatarButton(ActionEvent event) {
        // dialog for user to choose a file to upload
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Resource File");
        File selectedFile = chooser.showOpenDialog(editAvatar.getScene().getWindow());

        System.out.println(selectedFile.getAbsolutePath());

        String path = selectedFile.getAbsolutePath();



        // check if user chose an image
        if(!ImageManager.isImage(path)){
            // todo notify user of this
            System.out.println("Chosen file is not an image");
            return;
        }

        // we convert image to bytes
        byte[] image = ImageManager.convertImageToBytes(path);

        // we send message to server
        clientSocket.sendMessageToServer(new UserWantToEditProfile("UserWantToEditProfile", clientSocket.getDisplayName(), image, ""));
    }

    @FXML
    void editPasswordButton(ActionEvent event) {
        // todo
    }

    @FXML
    void editDisplayNameButton(ActionEvent e) {
        displayNameDialog.getEditor().setText(clientSocket.getDisplayName());
        displayNameDialog.setTitle("Edit Profile");
        displayNameDialog.setHeaderText("Enter name");
        displayNameDialog.setContentText("New name:");
        displayNameDialog.setGraphic(null);

        displayNameDialog.getDialogPane().setPrefWidth(400.0);

        final Button okButton = (Button) displayNameDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(false);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    String textInput = displayNameDialog.getEditor().getText();

                    // name can't be empty
                    if (textInput.isEmpty()) {
                        displayNameDialog.setContentText("Name can't be empty!");
                        event.consume();
                    }

                    // name can't exceed 24 characters
                    else if (textInput.length() > 24) {
                        displayNameDialog.setContentText("Name can't exceed 24 characters!");
                        event.consume();
                    }
                    // name can't start or end with a space (' ')
                    else if (textInput.substring(0, 1).equals(" ") || textInput.substring(textInput.length() - 1).equals(" ")) {
                        displayNameDialog.setContentText("Name can't start or end with a ' '");
                        event.consume();
                    }
                    // if name remains unchanged
                    else if (clientSocket.getDisplayName().equals(textInput)) {
                        event.consume();
                    }

                    // disable button until we have received answer
                    okButton.setDisable(true);
                    displayNameDialog.setContentText("Waiting for server...");
                    // send message to server
                    clientSocket.sendMessageToServer(new UserWantToEditProfile("UserWantToEditProfile", textInput, imageString, ""));
                    event.consume();
                    return;

                    // wait for response from server to verify if we did change displayname or not
                    //while(true){

                    //}
                }
        );

        displayNameDialog.showAndWait();
    }

    @Override
    public void userWantToEditProfileResponseEvent(UserWantToEditProfileResponse response) {
        // if user tried to edit displayname
        Platform.runLater(() ->{
            if(displayNameDialog.isShowing()){
                if(response.isChanged()){       // success
                    displayNameDialog.close();
                } else {
                    final Button okButton = (Button) displayNameDialog.getDialogPane().lookupButton(ButtonType.OK);
                    displayNameDialog.setContentText(response.getResponse());
                    okButton.setDisable(false);
                }
            }
        });
    }
}
