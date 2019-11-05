package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import no.ntnu.imt3281.ludo.client.ClientSocket;

public class LudoController {

    @FXML
    private MenuItem random;

    @FXML
    private TabPane tabbedPane;

	private ClientSocket clientSocket;

	// controllers
    private LoginController loginController;

	@FXML
	public void initialize(){
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
		loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

		clientSocket = new ClientSocket();

		try {
			AnchorPane loginBoard = loader.load();
			Tab tab = new Tab("Login");
			tab.setContent(loginBoard);
			tabbedPane.getTabs().add(tab);
			loginController = loader.getController();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// at last, try to create connection to server
		loginController.setClientSocket(clientSocket);
	}

    @FXML
    public void joinRandomGame(ActionEvent e) {  	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
    	loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

		GameBoardController controller = loader.getController();
		// Use controller to set up communication for this game.
		// Note, a new game tab would be created due to some communication from the server
		// This is here purely to illustrate how a layout is loaded and added to a tab pane.
		
    	try {
    		AnchorPane gameBoard = loader.load();
        	Tab tab = new Tab("Game");
    		tab.setContent(gameBoard);
        	tabbedPane.getTabs().add(tab);
    	} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
}
