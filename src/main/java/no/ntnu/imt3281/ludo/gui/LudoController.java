package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
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
import no.ntnu.imt3281.ludo.gui.ServerListeners.*;
import no.ntnu.imt3281.ludo.logic.messages.*;

public class LudoController implements ChatJoinResponseListener, LoginResponseListener, CreateGameResponseListener,
        SendGameInvitationsResponseListener, UserJoinedGameResponseListener, UserWantToViewProfileResponseListener {

    @FXML
    private MenuItem random;
    @FXML
    private MenuItem connect;
    @FXML
    private MenuItem joinRoom;
    @FXML
    private MenuItem challengeButton;
    @FXML
    private MenuItem myProfile;
    @FXML
    private MenuItem otherProfiles;
    @FXML
    private MenuItem about;

    @FXML
    private TabPane tabbedPane;

    private ClientSocket clientSocket;

    // controllers
    private LoginController loginController = null;
    private JoinChatRoomController joinChatRoomController = null;
    private SearchForPlayersController searchForPlayersController = null;
    private HashMap<String, GameBoardController> gameBoardControllers = new HashMap<>();  // k = ludoId
    private HashMap<String, ViewProfileController> viewProfileControllers = new HashMap<>(); // k = userid


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
        clientSocket.addCreateGameResponseListener(this);
        clientSocket.addSendGameInvitationsResponseListener(this);
        clientSocket.addUserJoinedGameResponseListener(this);
        clientSocket.addUserWantToViewProfileResponseListener(this);
    }

    /**
     * When user wants to join a new chatroom
     *
     * @param e
     */
    @FXML
    public void joinChatRoom(ActionEvent e) {
        // Here we just launch a new window where the user can write in his values
        FXMLLoader loader = new FXMLLoader(getClass().getResource("JoinChatRoom.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        try {
            Parent root = (Parent) loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Join Chat Room");
            stage.setScene(new Scene(root));
            stage.show();

            joinChatRoomController = loader.getController();
            joinChatRoomController.setClientSocket(clientSocket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Creates a new chat tab for our client
     */
    private void addNewChatTab(String chatName, ChatJoinResponse message) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatRoom.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        Tab chatTab = addNewTab(loader, chatName);
        if (chatTab == null) return;
        chatTab.setStyle("-fx-text-base-color: red");

        ChatRoomController controller = loader.getController();
        controller.setup(clientSocket, chatName, message.getChatlog(), message.getUsersinroom());

        // set listener for when user closes chat tab (except global chat)
        if (!chatName.equals("Global")) {
            chatTab.setOnClosed(controller.onTabClose);
            chatTab.setClosable(true);
        } else {
            // user can't close global chat tab
            chatTab.setClosable(false);
        }
    }

    private void addNewGameTab(String gameName, String gameId, String[] players) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        Tab gameTab = addNewTab(loader, gameName);

        GameBoardController controller = loader.getController();
        controller.setup(clientSocket, gameId);

        // add all the other players to this ludogame
        controller.setPlayers(players);
        gameTab.setOnClosed(controller.onTabClose);     // set method for when user closes tab

        // add to hashmap
        gameBoardControllers.put(gameId, controller);
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
                    // automatically focus on new tab when created
                    SingleSelectionModel<Tab> selectionModel = tabbedPane.getSelectionModel();
                    selectionModel.select(tab);
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

    /**
     * If user wants to join a random game
     *
     * @param e
     */
    @FXML
    public void joinRandomGame(ActionEvent e) {
        // send message to server that we want to join a random game
        clientSocket.sendMessageToServer(new UserDoesRandomGameSearch("UserDoesRandomGameSearch", clientSocket.getUserId()));
    }

    @FXML
    void challengePlayers(ActionEvent event) {
        // if this tab already exists remove it first
        if (searchForPlayersController != null) {
            tabbedPane.getTabs().remove(tabbedPane.getTabs().stream().filter(tab -> tab.getText() == "SearchForPlayers").findFirst().get());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchForPlayers.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        Tab searchForPlayersTab = addNewTab(loader, "SearchForPlayers");

        searchForPlayersController = loader.getController();
        searchForPlayersController.setup(clientSocket);
    }

    /**
     * List all available chatrooms the user can join
     *
     * @param e
     */
    @FXML
    public void listChatRooms(ActionEvent e) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatRoomsList.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        Tab chatRoomsTab = addNewTab(loader, "Chat Rooms");

        ChatRoomsListController chatRoomsListController = loader.getController();
        chatRoomsListController.setup(clientSocket);

        // notify server that we want to get the list of available chatrooms
        clientSocket.sendMessageToServer(new UserListChatrooms("UserListChatrooms"));
    }


    @FXML
    void findProfile(ActionEvent event) {
        // todo
    }

    @FXML
    void viewMyProfile(ActionEvent event) {
        // send message to server about viewing our profile
        clientSocket.sendMessageToServer(new UserWantToViewProfile("UserWantToViewProfile", clientSocket.getDisplayName()));
    }

    @FXML
    public void aboutHelp(ActionEvent e) {
        //todo
    }


    /**
     * EVENT LISTENERS FOR RECEIVING MESSAGES FROM SERVER
     */

    /**
     * When we got response from server when user wants to join a chat room
     *
     * @param response the Message object we got from server
     */
    @Override
    public void chatJoinResponseEvent(ChatJoinResponse response) {

        // send message as long as the user has the popup window still open
        if (joinChatRoomController != null) {
            // if user could not join chat room, display an error message to user
            if (!response.isStatus()) {
                joinChatRoomController.setResponseMessage(response.getResponse(), true);
                return;
            } else {
                joinChatRoomController.setResponseMessage(response.getResponse(), false);
            }
        }


        // if success, we add the chat tab to the user's tab pane
        if (response.isStatus()) {
            // todo change tab colour instead of having a prefix for chat
            addNewChatTab(response.getChatroomname(), response);
        }
    }

    // todo ikke set X på login tab

    /**
     * When user has logged in successfully
     *
     * @param response the event message we got from server with relevant information
     */
    @Override
    public void loginResponseEvent(LoginResponse response) {
        if (loginController != null) {
            loginController.setLoginResponseMessage(response.getResponse(), response.isLoginStatus());
        }

        // if user did not manage to log in
        if (!response.isLoginStatus()) {
            return;
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

    /**
     * When client has gotten answer that a new ludo game has been created
     *
     * @param response the event message we got from server with relevant info
     */
    @Override
    public void createGameResponseEvent(CreateGameResponse response) {
        // close the "Search For Players" tab
        if (searchForPlayersController != null) {
            Platform.runLater(() -> {
                tabbedPane.getTabs().remove(tabbedPane.getTabs().stream().filter(tab -> tab.getText() == "SearchForPlayers").findFirst().get());
            });
            searchForPlayersController = null;
        }

        // Something went wrong
        if (!response.isJoinstatus()) {
            // todo show message to user
            return;
        }

        // add the new game tab
        // todo change name of game tab for each game created
        addNewGameTab("Game", response.getGameid(), new String[]{clientSocket.getDisplayName()});
    }

    /**
     * When this client receives an invite to a game show a dialog with the invitation
     *
     * @param response the inviter and other information in an object response from server
     */
    @Override
    public void sendGameInvitationsResponseEvent(SendGameInvitationsResponse response) {
        // show the dialog to user
        Platform.runLater(() -> {
            new GameInvitationAlert(response.getHostdisplayname(), response.getGameid(), clientSocket);
        });
    }

    /**
     * When a new user has accepted the invitation and prepares to join the lobby
     *
     * @param response
     */
    @Override
    public void userJoinedGameResponseEvent(UserJoinedGameResponse response) {
        // if a gameboard of this game exists
        if (gameBoardControllers.get(response.getGameid()) != null) {
            gameBoardControllers.get(response.getGameid()).setPlayers(response.getPlayersinlobby());
        } else {
            // create a new game tab with the players currently in lobby
            // todo change name of game tab for each game created
            addNewGameTab("Game", response.getGameid(), response.getPlayersinlobby());
        }
    }

    @Override
    public void userWantToViewProfileResponseEvent(UserWantToViewProfileResponse response) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewProfile.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));
        ViewProfileController controller = viewProfileControllers.get(response.getUserId());

        // if tab already exists, reload all data inside and focus on it
        if (controller != null) {
            controller.setup(clientSocket, response, response.getUserId().equals(clientSocket.getUserId()));    // reload data

            Optional<Tab> foundTab = tabbedPane.getTabs().stream().filter(tab -> tab.getId() != null && tab.getId().equals("Profile-" + response.getUserId())).findFirst();   // find the tab
            if (!foundTab.isEmpty() && foundTab.isPresent()) {
                Platform.runLater(() -> {
                    foundTab.get().setText("Profile: " + response.getDisplayName());      // change tab name
                    tabbedPane.getSelectionModel().select(foundTab.get());                // focus to tab
                });
            } else {    // controller exists, but no tab. We remove controller and we call this function again to start fresh
                Platform.runLater(() -> {
                    viewProfileControllers.remove(response.getUserId());
                    userWantToViewProfileResponseEvent(response);
                });
            }
            return;
        }

        Tab tab = addNewTab(loader, "Profile: " + response.getDisplayName());
        // we set the id of the tab
        tab.setId("Profile-" + response.getUserId());

        controller = loader.getController();
        controller.setup(clientSocket, response, response.getUserId().equals(clientSocket.getUserId()));
        // add to hashmap for later retrieval
        viewProfileControllers.put(response.getUserId(), controller);
    }
}