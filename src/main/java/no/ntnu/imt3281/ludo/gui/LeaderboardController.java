package no.ntnu.imt3281.ludo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.gui.ServerListeners.LeaderboardResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.LeaderboardResponse;
import no.ntnu.imt3281.ludo.server.TopTenList;

public class LeaderboardController implements LeaderboardResponseListener {
    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<TopTenList.WonEntry> topWinsList;
    @FXML
    private ListView<TopTenList.PlayedEntry> topPlaysList;

    public void setup(ClientSocket clientSocket){
        clientSocket.addLeaderboardResponseListener(this);
    }

    @FXML
    public void initialize(){
        // configure the cell for each row
        topWinsList.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(TopTenList.WonEntry item, boolean empty) {
                super.updateItem(item, empty);
                if(empty) {
                    setGraphic(null);
                } else {
                    StackPane pane = new StackPane();
                    pane.setAlignment(Pos.CENTER_LEFT);

                    Label place = new Label();
                    place.setText(String.valueOf(item.getPlace()));
                    place.setFont(Font.font(20));
                    place.setTranslateX(10);

                    Label name = new Label();
                    name.setText(String.valueOf(item.getPlayerName()));
                    name.setFont(Font.font(20));
                    name.setTranslateX(120);

                    Label count = new Label();
                    count.setText(String.valueOf(item.getWonCount()));
                    count.setFont(Font.font(20));
                    count.setTranslateX(385);

                    pane.getChildren().addAll(place, name, count);
                    setGraphic(pane);
                }
            }
        });

        topPlaysList.setCellFactory(cell -> new ListCell<>() {
            @Override
            protected void updateItem(TopTenList.PlayedEntry item, boolean empty) {
                super.updateItem(item, empty);
                if(empty) {
                    setGraphic(null);
                } else {
                    StackPane pane = new StackPane();
                    pane.setAlignment(Pos.CENTER_LEFT);

                    Label place = new Label();
                    place.setText(String.valueOf(item.getPlace()));
                    place.setFont(Font.font(20));
                    place.setTranslateX(10);

                    Label name = new Label();
                    name.setText(String.valueOf(item.getPlayerName()));
                    name.setFont(Font.font(20));
                    name.setTranslateX(120);

                    Label count = new Label();
                    count.setText(String.valueOf(item.getPlayedCount()));
                    count.setFont(Font.font(20));
                    count.setTranslateX(385);

                    pane.getChildren().addAll(place, name, count);
                    setGraphic(pane);
                }
            }
        });

        // set height of listviews to fit exactly 10 entries
        topWinsList.setFixedCellSize(splitPane.getPrefHeight() / 10.0);
        topPlaysList.setFixedCellSize(splitPane.getPrefHeight() / 10.0);
    }

    /**
     * When we got the leaderboard request from the server
     * @param response
     */
    @Override
    public void leaderboardResponseEvent(LeaderboardResponse response) {
        // set the listview for top wins
        ObservableList<TopTenList.WonEntry> wonItems = FXCollections.observableArrayList(response.getToptenwins());
        topWinsList.setItems(wonItems);

        // set the listview for top plays
        ObservableList<TopTenList.PlayedEntry> playsItems = FXCollections.observableArrayList(response.getToptenplays());
        topPlaysList.setItems(playsItems);
    }
}
