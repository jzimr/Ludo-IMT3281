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
    void editDisplayNameButton(ActionEvent e) {
        // create a new dialog that contains our name in the field already
        TextInputDialog dialog = new TextInputDialog("Whatever");

        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Enter name");
        dialog.setContentText("New name:");
        dialog.setGraphic(null);

        dialog.getDialogPane().setPrefWidth(400.0);

        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    String textInput = dialog.getEditor().getText();

                    // name can't be empty
                    if(textInput.isEmpty()){
                        dialog.setContentText("Name can't be empty!");
                        event.consume();
                    }

                    // name can't exceed 24 characters
                    else if(textInput.length() > 24) {
                        dialog.setContentText("Name can't exceed 24 characters!");
                        event.consume();
                    }
                    // name can't start or end with a space (' ')
                    else if(textInput.substring(0, 1).equals(" ") || textInput.substring(textInput.length() - 1).equals(" ")){
                        dialog.setContentText("Name can't start or end with a ' '");
                        event.consume();
                    }
                    // if name remains unchanged
                    else if(textInput.equals(textInput)){
                        return;
                    }

                    // disable button until we have received answer
                    //okButton.setDisable(true);
                    // send message to server
                    //while wait for response
                    // if response ok == close window
                    // else show another dialog with more info
                }
        );

        dialog.showAndWait();
    }

    // todo add listener method
}
