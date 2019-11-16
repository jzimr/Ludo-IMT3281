package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.event.EventDispatchChain;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.UserWantToEditProfileResponseListener;

import java.util.Optional;

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
    private Text playedText;

    @FXML
    private Text wonText;



    public void setup(ClientSocket clientSocket){
        // todo: fix the profile displayname, image, etc.
    }

    @FXML
    void editAvatarButton(ActionEvent event) {

    }

    @FXML
    void editDisplayNameButton(ActionEvent event) {
        // create a new dialog that contains our name in the field already
        TextInputDialog dialog = new TextInputDialog(displayNameText.getText());

        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Enter name");
        dialog.setContentText("New name:");
        dialog.setGraphic(null);

        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                e -> {
                    // disable button until we have received answer
                    okButton.setDisable(true);

                    // send message to server
                    //while wait for response
                    // if response ok == close window
                    // else show another dialog with more info
                }
        );
    }

    // todo add listener method
}
