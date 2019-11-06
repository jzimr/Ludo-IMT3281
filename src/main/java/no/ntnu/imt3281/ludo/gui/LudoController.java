package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.ClientSocket;
import no.ntnu.imt3281.ludo.client.SessionTokenManager;
import no.ntnu.imt3281.ludo.gui.ServerListeners.ChatJoinResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.LoginResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.ChatJoinResponse;
import no.ntnu.imt3281.ludo.logic.messages.ClientLogin;
import no.ntnu.imt3281.ludo.logic.messages.LoginResponse;
import no.ntnu.imt3281.ludo.logic.messages.UserJoinChat;

public class LudoController implements ChatJoinResponseListener, LoginResponseListener {

    @FXML
    private MenuItem random;
    @FXML
    private MenuItem connect;
    @FXML
	private MenuItem joinRoom;

    @FXML
    private TabPane tabbedPane;

    private ClientSocket clientSocket;

    private Stage joinChatRoomPopup = null;

    // controllers
    private LoginController loginController = null;
    private JoinChatRoomController joinChatRoomController = null;
    ArrayList<ChatController> chatControllers = new ArrayList<>();
    // todo gameboardcontrollers


    @FXML
    public void initialize() {
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        clientSocket = new ClientSocket();

        // Create the "Login" tab
        Tab loginTab = addNewTab(loader, "Login");
        loginController = loader.getController();
        // at last, pass clientSocket to the controller
        loginController.setClientSocket(clientSocket);

        // if user has a session file stored locally (i.e. he did "Remember me" in a previous login)
        // then login automatically
        SessionTokenManager.SessionData sessionData = SessionTokenManager.readSessionFromFile();
        if (sessionData != null) {
            String ip = sessionData.serverAddress.split(":")[0];
            String port = sessionData.serverAddress.split(":")[1];

            boolean success = loginController.connectToServer(ip, port);

            // We only return on success, else something wrong has happened (wrong token, server IP, server not available, etc.).
            // In that case we redirect user back to a login screen
            if (success) {
                // send a unique session token to the server so it knows it's us
                ClientLogin login = new ClientLogin("UserDoesLoginAuto");
                // send the stored session token
                login.setRecipientSessionId(sessionData.sessionToken);
                // send auto login message to server
                clientSocket.sendMessageToServer(login);
            }
        }

        // set listeners
        clientSocket.addLoginResponseListener(this);
        clientSocket.addChatJoinResponseListener(this);
    }

	/**
	 * When user wants to join a new chatroom
	 * @param e
	 */
	@FXML
	public void joinChatRoom(ActionEvent e){
	    // Here we just launch a new window where the user can write in his values
		FXMLLoader loader = new FXMLLoader(getClass().getResource("JoinChatRoom.fxml"));
		loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

		try{
			Parent root = (Parent) loader.load();
			Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Join Chat Room");
			stage.setScene(new Scene(root));
            stage.show();
            // we need to save stage so we can close it later
            joinChatRoomPopup = stage;

            joinChatRoomController = loader.getController();
            joinChatRoomController.setClientSocket(clientSocket);
        } catch(IOException ex){
			ex.printStackTrace();
		}
	}


    /**
     * Creates a new chat tab for our client
     */
    private void addNewChatTab(String chatName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Chat.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        Tab chatTab = addNewTab(loader, chatName);
        if (chatTab == null) return;

        ChatController controller = loader.getController();
        // set listener for when user closes chat tab (except global chat)
        if(chatName != "Global"){
            chatTab.setOnClosed(controller.onTabClose);
        }
        // add to this list so we can correctly delegate messages to the correct channels when we get them
        chatControllers.add(controller);
    }

    /**
     * Create a new tab and attach it to our "tabbedPane" manager
     *
     * @param loader  FXMLLoader
     * @param tabName the name of the new tab
     * @return the Tab that was created
     */
    private Tab addNewTab(FXMLLoader loader, String tabName) {
        try {
            AnchorPane chat = loader.load();
            Tab tab = new Tab(tabName);
            tab.setContent(chat);

            // we need to execute on JavaFX thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    tabbedPane.getTabs().add(tab);
                    focusOnTab(tab);        // automatically focus on new tab when created
                }
            });
            return tab;
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }
    }


    /**
     * User wants to connect to another server or change user or register as new user or disable auto-login
     */
    @FXML
    public void connectToServer(ActionEvent e) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        // switch to login tab if it exists
        for (Tab tab : tabbedPane.getTabs()) {
            if (tab.getText() == "Login") {
                tabbedPane.getSelectionModel().select(tab);
                return;
            }
        }

        // if not create a new one
        Tab loginTab = addNewTab(loader, "Login");
        loginController = loader.getController();
        // pass clientSocket to the controller
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

    /**
     * Helper function to focus on a specific tab
     * @param tab the tab we want to focus on
     */
    public void focusOnTab(Tab tab){
        SingleSelectionModel<Tab> selectionModel = tabbedPane.getSelectionModel();
        selectionModel.select(tab);
    }


    /**
     * EVENT LISTENERS FOR RECEIVING MESSAGES FROM SERVER
     */

    /**
     * When we got response from server when user wants to join a chat room
     * @param response the Message object we got from server
     */
    @Override
    public void chatJoinResponseEvent(ChatJoinResponse response) {

        // send message as long as the user has the popup window still open
        if(joinChatRoomController != null){
            // if user could not join chat room, display an error message to user
            if(!response.isStatus()){
                joinChatRoomController.setResponseMessage(response.getResponse() + " (For room '" + /* todo room */ "')", true);
                return;
            } else {
                joinChatRoomController.setResponseMessage(response.getResponse()+ " (For room '" + /* todo room */ "')", false);
            }
        }

        // if success, we add the chat tab to the user's tab pane
        if(response.isStatus()){
            // todo change name to real chat name
            // todo change tab colour instead of having a prefix for chat
            addNewChatTab("<Chat> Global");
        }
    }

    // todo ikke set X på login tab

    /**
     * When user has logged in successfully
     * @param response the event message we got from server with relevant information
     */
    @Override
    public void loginResponseEvent(LoginResponse response) {
        if(loginController != null){
            loginController.setLoginResponseMessage(response.getResponse(), response.isLoginStatus());
        }

        // we automatically add the client to the global chat room
        clientSocket.sendMessageToServer(new UserJoinChat("UserJoinChat", "Global", clientSocket.getUserId()));

        // we need to execute on JavaFX thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Tab loginTab = tabbedPane.getTabs().stream().filter(n -> n.getText().equals("Login")).findFirst().get();
                tabbedPane.getTabs().remove(loginTab);
            }
        });
    }
}
