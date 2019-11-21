package no.ntnu.imt3281.ludo.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.logic.messages.UserDoesGameInvitationAnswer;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Upon creation shows an JavaFX Alert Dialog to the user with an invitation to a new game which
 * he then can accept of decline.
 */
public class GameInvitationAlert extends Alert {
    private int timeLeft = 40;      // how many seconds the user has to answer the invite
    private ResourceBundle i18Bundle;

    public GameInvitationAlert(String hostName, String gameId, ClientSocket clientSocket) {
        super(AlertType.CONFIRMATION);

        // for i18n
        Locale locale = Locale.getDefault();
        i18Bundle = ResourceBundle.getBundle("no.ntnu.imt3281.I18N.Game", locale);

        // minimal stuff in alert box
        initStyle(StageStyle.UTILITY);

        // set text to show
        setTitle(i18Bundle.getString("invitation.new"));
        setHeaderText(null);
        setContentText("'" + hostName + "' " + i18Bundle.getString("invitation.inviteMsg") + " " + timeLeft);

        // set buttons text
        ((Button) getDialogPane().lookupButton(ButtonType.OK)).setText(i18Bundle.getString("invitation.accept"));
        ((Button) getDialogPane().lookupButton(ButtonType.CANCEL)).setText(i18Bundle.getString("invitation.decline"));

        // play a JavaFX animation to show the user how much time he has left to answer
        Timeline twoSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                timeLeft = timeLeft - 1;
                setContentText("'" + hostName + "' " + i18Bundle.getString("invitation.inviteMsg") + " " + timeLeft);

                // when time has reached 0 we close the alert dialog
                if(timeLeft == 0){
                    // send message to server that the request was declined
                    clientSocket.sendMessageToServer(new UserDoesGameInvitationAnswer("UserDoesGameInvitationAnswer",
                            false, clientSocket.getUserId(), gameId));
                    close();
                }
            }
        }));
        twoSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        twoSecondsWonder.play();

        // show dialog and wait for answer
        Optional<ButtonType> result = showAndWait();

        // stop the playback
        twoSecondsWonder.stop();

        // User pressed "Accept"
        if(result.get() == ButtonType.OK){
            clientSocket.sendMessageToServer(new UserDoesGameInvitationAnswer("UserDoesGameInvitationAnswer",
                    true, clientSocket.getUserId(), gameId));
        } else {
            // if user closed the window or pressed "Decline"
            clientSocket.sendMessageToServer(new UserDoesGameInvitationAnswer("UserDoesGameInvitationAnswer",
                    false, clientSocket.getUserId(), gameId));
        }
    }
}
