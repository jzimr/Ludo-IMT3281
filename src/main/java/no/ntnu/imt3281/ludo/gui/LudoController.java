package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.client.SessionTokenManager;
import no.ntnu.imt3281.ludo.gui.ServerListeners.LoginResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.ClientLogin;
import no.ntnu.imt3281.ludo.logic.messages.LoginResponse;

public class LudoController {

    @FXML
    private MenuItem random;
    @FXML
	private MenuItem connect;

    @FXML
    private TabPane tabbedPane;

	private ClientSocket clientSocket;

	// controllers
    private LoginController loginController;

	@FXML
	public void initialize(){
		tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
		loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

		clientSocket = new ClientSocket();

		// Create the "Login" tab
		try {
			AnchorPane loginBoard = loader.load();
			Tab tab = new Tab("Login");
			tab.setContent(loginBoard);
			tabbedPane.getTabs().add(tab);
			loginController = loader.getController();
			// at last, pass clientSocket to the controller
			loginController.setClientSocket(clientSocket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// if user has a session file stored locally (i.e. he did "Remember me" in a previous login)
		// then login automatically
		SessionTokenManager.SessionData sessionData = SessionTokenManager.readSessionFromFile();
		if(sessionData != null){
			String ip = sessionData.serverAddress.split(":")[0];
			String port = sessionData.serverAddress.split(":")[1];

			boolean success = connectToServer(ip, port);

			// We only return on success, else something wrong has happened (wrong token, server IP, server not available, etc.).
			// In that case we delete the file and go on and redirect user back to a login screen
			if(success){
				// send a unique session token to the server so it knows it's us
				ClientLogin login = new ClientLogin("UserDoesLoginAuto");
				// send the stored session token
				login.setRecipientSessionId(sessionData.sessionToken);
				// send auto login message to server
				clientSocket.sendMessageToServer(login);
			}
		}
	}

	/**
	 * User wants to connect to another server or change user or register as new user or disable auto-login
	 */
	@FXML
	public void connectToServer(ActionEvent e){
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
		loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

		// switch to login tab if it exists
		for(Tab tab : tabbedPane.getTabs()){
			if(tab.getText() == "Login"){
				tabbedPane.getSelectionModel().select(tab);
				return;
			}
		}

		// if not create a new one
		try {
			AnchorPane gameBoard = loader.load();
			Tab tab = new Tab("Login");
			tab.setContent(gameBoard);
			tabbedPane.getTabs().add(tab);
			loginController = loader.getController();
			// pass clientSocket to the controller
			loginController.setClientSocket(clientSocket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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

	/**
	 * Try to connect to server using ip and port which user entered
	 *
	 * @param ip   the IP address to the server
	 * @param port the port to the server
	 * @return if connection to server could be made (true), or not (false)
	 */
	boolean connectToServer(String ip, String port) {
		// try to connect to the server
		boolean success;

		try {
			success = clientSocket.establishConnectionToServer(ip, Integer.parseInt(port));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			// wrong port probably, notify client
			return false;
		}

		// no connection to server
		if (!success) {
			return false;
		}

		// all gucci
		return true;
	}
}
