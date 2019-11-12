package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.GameHasStartedResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.UserLeftGameResponseListener;
import no.ntnu.imt3281.ludo.logic.*;
import no.ntnu.imt3281.ludo.logic.messages.UserLeftGame;
import no.ntnu.imt3281.ludo.logic.messages.UserLeftGameResponse;

import java.util.Arrays;

public class GameBoardController implements UserLeftGameResponseListener, DiceListener, PieceListener, PlayerListener {

    @FXML
    private GridPane homeGrid;

    @FXML
    private ImageView redPiece0;

    @FXML
    private ImageView redPiece01;

    @FXML
    private ImageView redPiece02;

    @FXML
    private ImageView redPiece03;

    @FXML
    private ImageView bluePiece0;

    @FXML
    private ImageView bluePiece1;

    @FXML
    private ImageView bluePiece2;

    @FXML
    private ImageView bluePiece3;

    @FXML
    private ImageView yellowPiece0;

    @FXML
    private ImageView yellowPiece1;

    @FXML
    private ImageView yellowPiece2;

    @FXML
    private ImageView yellowPiece3;

    @FXML
    private ImageView greenPiece0;

    @FXML
    private ImageView greenPiece1;

    @FXML
    private ImageView greenPiece2;

    @FXML
    private ImageView greenPiece3;

    @FXML
    private GridPane movingGrid;

    @FXML
    private ImageView player1Active;

    @FXML
    private Label player1Name;

    @FXML
    private ImageView player2Active;

    @FXML
    private Label player2Name;

    @FXML
    private ImageView player3Active;

    @FXML
    private Label player3Name;

    @FXML
    private ImageView player4Active;

    @FXML
    private Label player4Name;

    @FXML
    private ImageView diceThrown;

    @FXML
    private Button throwTheDice;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField textToSay;

    @FXML
    private Button sendTextButton;

    private ClientSocket clientSocket;
    private Ludo ludoGame;
    private String gameId;
    private String[] players = new String[]{};

    /**
     * Setup necessary stuff for this object
     *
     * @param clientSocket for client-server communcation
     */
    public void setup(ClientSocket clientSocket, String gameId) {
        this.clientSocket = clientSocket;
        this.gameId = gameId;

        // initiate a new LudoGame object
        ludoGame = new Ludo();

        // add listeners
        clientSocket.addUserLeftGameResponseListener(this);
    }

    /**
     * Add a list of players to the ludo game
     *
     * @param players the players in the game
     */
    public void setPlayers(String[] players) {
        // for each player that has not been added yet
        for (int i = this.players.length; i < players.length; i++) {
            ludoGame.addPlayer(players[i]);
        }

        // update our list here
        this.players = players;

        // set the text in GUI
        Platform.runLater(() -> {
            int playerCount = players.length;

            if (playerCount > 0) {
                player1Name.setText(players[0]);
            }
            if (playerCount > 1) {
                player2Name.setText(players[1]);
            }
            if (playerCount > 2) {
                player3Name.setText(players[2]);
            }
            if (playerCount > 3) {
                player3Name.setText(players[3]);
            }
        });
    }


    @Override
    public void userLeftGameResponseEvent(UserLeftGameResponse response) {
        ludoGame.removePlayer(response.getDisplayname());

        // todo handle chat as well

        // update the text in GUI
        Platform.runLater(() -> {
            int playerCount = players.length;

            if (playerCount > 0 && response.getDisplayname().equals(players[0])) {
                player1Name.setOpacity(0.25);
            }
            if (playerCount > 1 && response.getDisplayname().equals(players[1])) {
                player2Name.setOpacity(0.25);
            }
            if (playerCount > 2 && response.getDisplayname().equals(players[2])) {
                player3Name.setOpacity(0.25);
            }
            if (playerCount > 3 && response.getDisplayname().equals(players[3])) {
                player4Name.setOpacity(0.25);
            }
        });
    }

    /**
     * Compare gameId in this object with gameId in parameter
     *
     * @param gameId the unique ID of the ludo game
     * @return if are equal or not
     */
    @Override
    public boolean equals(String gameId) {
        return this.gameId.equals(gameId);
    }

    /**
     * Called when the tab of this controller is closed.
     * Here we want to handle stuff like sending message to server.
     */
    public EventHandler<Event> onTabClose = new EventHandler<Event>() {
        @Override
        public void handle(Event arg0) {
            // remove this listener from clientsocket
            clientSocket.removeUserLeftGameResponseListener(GameBoardController.this);
            // disconnect user from the game
            clientSocket.sendMessageToServer(new UserLeftGame("UserLeftGame", gameId));
            // todo disconnect from the chat as well
        }
    };

    /*
    LUDO GAME LISTENERS
     */

    @Override
    public void diceThrown(DiceEvent diceEvent) {
        // todo
    }

    @Override
    public void pieceMoved(PieceEvent pieceEvent) {
        // todo
    }

    @Override
    public void playerStateChanged(PlayerEvent event) {
        // todo
    }
}
