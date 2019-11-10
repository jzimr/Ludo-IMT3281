package no.ntnu.imt3281.ludo.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.logic.messages.UserJoinedGameResponse;

public class GameBoardController {

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
    private String[] players;

    /**
     * Setup necessary stuff for this object
     *
     * @param clientSocket for client-server communcation
     */
    public void setup(ClientSocket clientSocket, String gameId) {
        this.clientSocket = clientSocket;
        this.gameId = gameId;
    }

    /**
     * Set the players of the ludo game (Setting all everytime as to be sure everyone has the same order of players)
     * @param players the players in the game
     */
    public void setPlayers(String[] players){
        this.players = players;

        // set the text of each player in the list
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int playerCount = players.length;

                if(playerCount > 0){
                    player1Name.setText(players[0]);
                } if (playerCount > 1){
                    player2Name.setText(players[1]);
                } if (playerCount > 2){
                    player3Name.setText(players[2]);
                } if (playerCount > 3){
                    player3Name.setText(players[3]);
                }
            }
        });
    }






}
