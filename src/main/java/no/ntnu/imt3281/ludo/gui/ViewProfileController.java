package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import no.ntnu.imt3281.ludo.Exceptions.InvalidImageException;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.client.ImageManager;
import no.ntnu.imt3281.ludo.gui.ServerListeners.UserWantToEditProfileResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.UserWantToViewProfileResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.*;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

public class ViewProfileController implements UserWantToEditProfileResponseListener, UserWantToViewProfileResponseListener {

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
    private ResourceBundle i18Bundle;

    // create a new dialog that contains our name in the field already
    TextInputDialog displayNameDialog;
    Dialog<Pair<String, String>> passwordDialog;

    // we save the image in form of bytes as well
    byte[] imageString;

    @FXML
    public void initialize() {
        Locale locale = Locale.getDefault();
        i18Bundle = ResourceBundle.getBundle("no.ntnu.imt3281.I18N.Game", locale);
    }

    /**
     * Setup this object with the necessary information
     * @param clientSocket
     * @param response
     * @param isOurProfile
     */
    public void setup(ClientSocket clientSocket, final UserWantToViewProfileResponse response, boolean isOurProfile) {
        this.clientSocket = clientSocket;

        // load all the data into the GUI
        Platform.runLater(() -> {
            // if user has set own image, else we use default
            if (response.getImageString() != null) {
                Image image = ImageManager.convertBytesToImage(response.getImageString());
                imageString = response.getImageString();
                // check that it decoded without problems
                if (image != null) {
                    avatarImage.setImage(image);
                }
            }

            // set all other fields
            displayNameText.setText(response.getDisplayName());
            userIdText.setText(response.getUserId());
            playedText.setText(String.valueOf(response.getGamesPlayed()));
            wonText.setText(String.valueOf(response.getGamesWon()));

            // if we view our own profile, we want to be able to edit it
            if (isOurProfile) {
                // register listener
                clientSocket.addUserWantToEditProfileResponseListener(this);

                // make buttons visible
                editDisplayName.setDisable(false);
                editDisplayName.setVisible(true);
                editPassword.setDisable(false);
                editPassword.setVisible(true);
                editAvatar.setDisable(false);
                editAvatar.setVisible(true);

                // setup dialog for changing display name
                displayNameDialog = new TextInputDialog(clientSocket.getDisplayName());
                displayNameDialog.setTitle(i18Bundle.getString("profile.windowTxt"));
                displayNameDialog.setHeaderText(i18Bundle.getString("profile.newNameTxt"));
                displayNameDialog.setGraphic(null);
                displayNameDialog.getDialogPane().setPrefWidth(400.0);

                // we create a custom password dialog with two fields
                passwordDialog = new Dialog();
                passwordDialog.setTitle(i18Bundle.getString("profile.windowTxt"));
                passwordDialog.setGraphic(null);
                passwordDialog.getDialogPane().setPrefWidth(400.0);

                // Set the button types.
                //ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                passwordDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                VBox vbox = new VBox();
                vbox.setPadding(new Insets(10, 80, 5, 80));
                vbox.setSpacing(10.0);

                PasswordField newPassword = new PasswordField();
                newPassword.setId("newPassword");
                newPassword.setPromptText(i18Bundle.getString("profile.newPassword"));
                PasswordField retypePassword = new PasswordField();
                retypePassword.setId("retypePassword");
                retypePassword.setPromptText(i18Bundle.getString("profile.retypePassword"));
                Text responseMessage = new Text();
                responseMessage.setStyle("-fx-font-weight: bold");

                vbox.getChildren().addAll(newPassword, retypePassword, responseMessage);

                passwordDialog.getDialogPane().setContent(vbox);
                // focus on newPassword field on default
                Platform.runLater(() -> newPassword.requestFocus());
            }
        });
    }

    /**
     * When user wants to edit his avatar image
     * <p>
     *     Pressing this opens up a dialog where user can
     *     choose an image to change to.
     * </p>
     * @param event
     */
    @FXML
    void editAvatarButton(ActionEvent event) {
        // dialog for user to choose a file to upload
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Resource File");
        File selectedFile = chooser.showOpenDialog(editAvatar.getScene().getWindow());

        // user cancelled and did not select a file
        if (selectedFile == null)
            return;

        String path = selectedFile.getAbsolutePath();   // get path of the file

        // check if user chose an image
        if (!ImageManager.isImage(path)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, i18Bundle.getString("msg.fileNotImage"), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // we convert image to bytes
        byte[] image = null;
        try {
            image = ImageManager.convertImageToBytes(path);
        } catch (InvalidImageException e) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, e.getMessage(), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // we send message to server
        clientSocket.sendMessageToServer(new UserWantToEditProfile("UserWantToEditProfile", clientSocket.getDisplayName(), image, ""));
    }

    /**
     * User wants to edit his password
     *
     * @param e
     */
    @FXML
    void editPasswordButton(ActionEvent e) {
        final Button okButton = (Button) passwordDialog.getDialogPane().lookupButton(ButtonType.OK);
        final PasswordField newPassword = (PasswordField) ((VBox) passwordDialog.getDialogPane().getContent()).getChildren().get(0);
        final PasswordField retypePassword = (PasswordField) ((VBox) passwordDialog.getDialogPane().getContent()).getChildren().get(1);
        final Text responseMessage = (Text) ((VBox) passwordDialog.getDialogPane().getContent()).getChildren().get(2);

        okButton.setDisable(false);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    String newPasswordText = newPassword.getText();
                    String retypePasswordText = retypePassword.getText();

                    // name can't be empty
                    if (newPasswordText.isEmpty() || retypePasswordText.isEmpty()) {
                        Platform.runLater(() -> {
                            responseMessage.setStyle("-fx-fill: red");
                            responseMessage.setText(i18Bundle.getString("msg.needBothFields"));
                        });
                        event.consume();
                        return;
                    }

                    // password fields do not match
                    else if (!newPasswordText.equals(retypePasswordText)) {
                        Platform.runLater(() -> {
                            responseMessage.setStyle("-fx-fill: red");
                            responseMessage.setText(i18Bundle.getString("msg.passNoMatch"));
                        });
                        event.consume();
                        return;
                    }

                    // password must be at least 8 characters
                    else if (newPasswordText.length() < 8) {
                        Platform.runLater(() -> {
                            responseMessage.setStyle("-fx-fill: red");
                            responseMessage.setText(i18Bundle.getString("msg.passLeastChar"));
                        });
                        event.consume();
                        return;
                    }

                    // disable button until we have received answer
                    okButton.setDisable(true);
                    Platform.runLater(() -> {
                        responseMessage.setStyle("-fx-fill: black");
                        responseMessage.setText(i18Bundle.getString("msg.waitingForServer"));
                    });
                    // send message to server
                    clientSocket.sendMessageToServer(new UserWantToEditProfile("UserWantToEditProfile",
                            clientSocket.getDisplayName(), imageString, newPasswordText));
                    event.consume();
                    return;
                }
        );

        passwordDialog.showAndWait();
    }

    /**
     * The user wants to edit his display name
     * <p>
     *     As of how it is setup, the new displayname has
     *     to be unique to be able to change it
     * </p>
     * @param e
     */
    @FXML
    void editDisplayNameButton(ActionEvent e) {
        displayNameDialog.getEditor().setText(clientSocket.getDisplayName());
        displayNameDialog.setContentText(i18Bundle.getString("profile.newNameContent"));

        final Button okButton = (Button) displayNameDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(false);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    String textInput = displayNameDialog.getEditor().getText();

                    // name can't be empty
                    if (textInput.isEmpty()) {
                        displayNameDialog.setContentText(i18Bundle.getString("msg.profileEmpty"));
                        event.consume();
                        return;
                    }

                    // name can't exceed 24 characters
                    else if (textInput.length() > 24) {
                        displayNameDialog.setContentText(i18Bundle.getString("msg.exceedNameChars"));
                        event.consume();
                        return;
                    }
                    // name can't start or end with a space (' ')
                    else if (textInput.substring(0, 1).equals(" ") || textInput.substring(textInput.length() - 1).equals(" ")) {
                        displayNameDialog.setContentText(i18Bundle.getString("msg.nameCantStartWith"));
                        event.consume();
                        return;
                    }
                    // if name remains unchanged
                    else if (clientSocket.getDisplayName().equals(textInput)) {
                        event.consume();
                        return;
                    }

                    // disable button until we have received answer
                    okButton.setDisable(true);
                    displayNameDialog.setContentText(i18Bundle.getString("msg.waitingForServer"));
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

    /**
     * We check if user successfully changed his profile. If not show message to user
     * @param response
     */
    @Override
    public void userWantToEditProfileResponseEvent(UserWantToEditProfileResponse response) {
        // success
        if (response.isChanged()) {
            Platform.runLater(() -> {
                if (displayNameDialog.isShowing()) displayNameDialog.close();
                if (passwordDialog.isShowing()) passwordDialog.close();
            });

            // register listener to update our profile page on success
            clientSocket.addUserWantToViewProfileResponseListener(this);
            // send message to server so we can refresh our page
            clientSocket.sendMessageToServer(new UserWantToViewProfile("UserWantToViewProfile", clientSocket.getDisplayName()));
            return;
        }

        // else something wrong happened
        Platform.runLater(() -> {
            if (!response.isChanged()) {

                // user tried to edit displayname
                if (displayNameDialog.isShowing()) {
                    final Button okButton = (Button) displayNameDialog.getDialogPane().lookupButton(ButtonType.OK);
                    displayNameDialog.setContentText(i18Bundle.getString(response.getResponse()));
                    okButton.setDisable(false);
                }

                // user tried to edit password
                if (passwordDialog.isShowing()) {
                    final Button okButton = (Button) passwordDialog.getDialogPane().lookupButton(ButtonType.OK);
                    final Text responseMessage = (Text) ((VBox) passwordDialog.getDialogPane().getContent()).getChildren().get(2);
                    responseMessage.setStyle("-fx-fill: red");
                    responseMessage.setText(i18Bundle.getString(response.getResponse()));
                    okButton.setDisable(false);
                }
            }
        });
    }

    /**
     * We use this listener to refresh our profile page when the user has changed something so he can instantly
     * see the changes.
     *
     * @param response
     */
    @Override
    public void userWantToViewProfileResponseEvent(UserWantToViewProfileResponse response) {
        // set data
        avatarImage.setImage(ImageManager.convertBytesToImage(response.getImageString()));
        displayNameText.setText(response.getDisplayName());
    }

    /**
     * Here we only want to get if we are viewing our own profile
     *
     * @param displayname
     * @return
     */
    @Override
    public boolean waitingForProfile(String displayname) {
        return clientSocket.getDisplayName().equals(displayname);
    }

    /**
     * Called when the tab of this controller is closed.
     * Here we want to handle stuff like sending message to server.
     */
    public EventHandler<Event> onTabClose = new EventHandler<Event>() {
        @Override
        public void handle(Event arg0) {
            // remove listeners from clientsocket
            clientSocket.removeUserWantToViewProfileResponseListener(ViewProfileController.this);
        }
    };
}
