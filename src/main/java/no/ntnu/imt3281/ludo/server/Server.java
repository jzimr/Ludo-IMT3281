package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.*;
import no.ntnu.imt3281.ludo.logic.messages.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This is the main class for the server.
 */
public class Server implements DiceListener, PieceListener, PlayerListener {

	final private int SERVER_PORT = 4567; //Server Port
	Database db; //Database

	ArrayList<Ludo> activeLudoGames = new ArrayList<>(); //ArrayList of active games.
	ArrayList<ChatRoom> activeChatRooms = new ArrayList<>(); //ArrayList of chat rooms.
	ArrayList<Invitations> pendingInvites = new ArrayList<>(); //ArrayList of pending invites.

	LinkedList<Client> clients = new LinkedList<>(); //LinkedList containing clients

	boolean stopping = false; //Boolean to stop the server

	ArrayBlockingQueue<Message> objectsToHandle = new ArrayBlockingQueue<>(100); //Queue for incoming messages

	ArrayBlockingQueue<Message> messagesToSend = new ArrayBlockingQueue<>(100); //Queue for outbound messages

	ArrayBlockingQueue<Client> disconnectedClients = new ArrayBlockingQueue<>(1000); //Queue for clients that is to be disconnected.

	public static void main(String[] args) {
		new Server(false); //Create a new server instance.
	}

	public Server(boolean testing){
		startServerThread();
		startListener();
		startHandlingActions();
		startSenderThread();
		if (!testing){
			sendPingMessage();
			db = Database.getDatabase();
		} else {
			db = Database.constructTestDatabase("jdbc:derby:./ludoTestDB");
		}
		startRemoveDisconnectedClientsThread();

		System.out.println("Ludo server is now listening at 0.0.0.0:"+SERVER_PORT);

		try {
			db.createGlobalChatroom(); //Create global chatroom
		} catch (SQLException e) {
			e.printStackTrace();
		}

		setUpChatRooms();
		System.out.println("Chatrooms: " + activeChatRooms.toString());

	}

	public void stopServer(){
		stopping = true;
	}

	/**
	 * Creates chatroom objects with names from the db.
	 */
	private void setUpChatRooms(){
		ArrayList<String> roomNames = db.getAllChatRooms();
		for (String name : roomNames) {
			activeChatRooms.add(new ChatRoom(name));
		}
	}

	/**
	 *
	 * This one handles new connections to the server.
	 *
	 */
	private void startServerThread() {
		Thread server = new Thread(() -> {
			try {
				ServerSocket server1 = new ServerSocket(SERVER_PORT);
				while (!stopping) {
					Socket s = server1.accept();
					try {
						Client c = new Client(s);
						synchronized (clients) {
							clients.add(c);
						}
					} catch (IOException e) {
						System.err.println("Unable to create client from "+s.getInetAddress().getHostName());
					}
				}
			} catch ( IOException e) {
				e.printStackTrace();
			}
		});

		server.start();
	}


	/**
	 *
	 * This one Gets information from clients.
	 *
	 */
	private void startListener() {
		JsonMessageParser parser = new JsonMessageParser();
		TimerTask checkActivity = new TimerTask() {
			@Override
			public void run() {
				synchronized (clients) {
					Iterator<Client> iterator = clients.iterator();
					while (iterator.hasNext()) {
						Client c = iterator.next();
						try {
							String msg = c.read();
							if (msg != null && (msg.contains("UserDoesLogin") || msg.contains("UserDoesRegister"))) {
								synchronized (objectsToHandle) {
									c.parseSessionid(msg);
									System.out.println("Connected user : " + c.getUuid() + " " + msg);
									Message toBeQueued = parser.parseJson(msg,c.getUuid());
									if (toBeQueued != null) { //Discard if it is null
										objectsToHandle.add(parser.parseJson(msg,c.getUuid())); //Add the object to queue for handling
									} else {
										System.out.println("DISCARDED MESSAGE : " + msg);
									}
								}
							} else if (msg != null && c.getUuid() != null) {
								synchronized (objectsToHandle) {
									Message toBeQueued = parser.parseJson(msg,c.getUuid());
									if (toBeQueued != null) {
										objectsToHandle.add(parser.parseJson(msg,c.getUuid())); //Add the object to queue for handling
									} else {
										System.out.println("DISCARDED MESSAGE : " + msg);
									}
								}

							}
						} catch (IOException e) {   // Exception while reading from client, assume client is lost
							// Do nothing, this is really not likely to happen
						}
					}
				}
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(checkActivity, 50L, 50L);
	}

	/**
	 * This sends data to the user
	 *
	 */
	private void startSenderThread() {
		Thread sender = new Thread(() -> {
			while (!stopping) {
				try {
					Message msg = messagesToSend.take();
					Iterator<Client> iterator = clients.iterator();
					while (iterator.hasNext()) {
						Client c = iterator.next();
						if (c.getUuid() != null && msg.getRecipientSessionId().contentEquals(c.getUuid())) {
							try {
								String converted = convertToCorrectJson(msg);
								System.out.println("Session id: " + c.getUuid() + " " + converted);
								c.send(converted);
							} catch (IOException e) {   // Exception while sending to client, assume client is lost
								synchronized (disconnectedClients) {
									if (!disconnectedClients.contains(c)) {
										disconnectedClients.add(c);
									}
								}
							}
						}
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		sender.setDaemon(true);
		sender.start();
	}

	/**
	 * This removes users that has disconnected
	 */
	private void startRemoveDisconnectedClientsThread() {
		Thread removeDisconnectedClientsThread = new Thread(() -> {
			while (!stopping) {
				try {
					Client client = disconnectedClients.take();
					synchronized (clients) {
						//Todo: Remove player from all chat rooms and games.
						removeClientsFromModules(client.getUserId());
						System.out.println("Removed client " + client.getUserId());
						clients.remove(client);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		removeDisconnectedClientsThread.setDaemon(true);
		removeDisconnectedClientsThread.start();
	}

	/**
	 * This is used to check if a client is still connected to the server.
	 */
	private void sendPingMessage(){
		Thread sendPingMessage = new Thread(() -> {
			while(!stopping) {
				LinkedList<Client> copyList = (LinkedList<Client>) clients.clone();
				Iterator<Client> clientIterator = copyList.iterator();
				while(clientIterator.hasNext()){
					Client c = clientIterator.next();
					try {
						c.send("{\"action\": \"Ping\"}");
					} catch (IOException e) {
						synchronized (clients) {
							disconnectedClients.add(c);
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		sendPingMessage.setDaemon(true);
		sendPingMessage.start();
	}

	/**
	 * This function removes a client from all chatrooms + games.
	 * @param userId
	 */
	private void removeClientsFromModules(String userId){
		//TODO: Remove from game

		//TODO: Send message that the user left to chat channels.

		//Remove user from chat rooms.
		ArrayList<ChatRoom> rooms = (ArrayList<ChatRoom>) activeChatRooms.clone();
			for(ChatRoom room : rooms){
				if(room.getConnectedUsers().contains(userId)){
					UserInfo info = db.getProfile(userId);
					announceRemovalToUsersInChatRoom(info, room.getName());
					removeUserFromChatroom(room.getName(), userId);
					System.out.println("We removed a boy");
				}

			}

	}

	/**
	 * Thread to handle incoming messages from users.
	 */
	private void startHandlingActions(){
		Thread handleActions = new Thread(() -> {
			while (!stopping) {
				try {
					Message message = objectsToHandle.take();
					System.out.println("Handle obj : " + message.getAction());
					handleAction(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		handleActions.start();
	}

	/**
	 * Determines what function is to be called by checking what action a message
	 * that comes from a client is.
	 * @param action
	 */
	private void handleAction(Message action){
		switch (action.getAction()) {
			case "UserDoesDiceThrow": UserDoesDiceThrow((UserDoesDiceThrow) action);break;
			case "UserDoesLoginManual": UserDoesLoginManual((ClientLogin) action);break;
			case "UserDoesLoginAuto": UserDoesLoginAuto((ClientLogin) action); break;
			case "UserDoesRegister": UserDoesRegister((ClientRegister) action); break;
			case "UserJoinChat": UserJoinChat((UserJoinChat) action); break;
			case "UserSentMessage": UserSentMessage((UserSentMessage) action); break;
			case "UserLeftChatRoom": UserLeftChatRoom((UserLeftChatRoom) action); break;
			case "UserListChatrooms": UserListChatrooms((UserListChatrooms) action); break;
            case "UserWantsUsersList": UserWantsUsersList((UserWantsUsersList) action); break;
			case "UserWantsToCreateGame": UserWantsToCreateGame((UserWantsToCreateGame) action); break;
			case "UserDoesGameInvitationAnswer": UserDoesGameInvitationAnswer((UserDoesGameInvitationAnswer) action); break;
			case "UserLeftGame": UserLeftGame((UserLeftGame) action); break;
			case "UserDoesPieceMove" : UserDoesPieceMove((UserDoesPieceMove) action); break;
			case "UserDoesRandomGameSearch" : UserDoesRandomGameSearch((UserDoesRandomGameSearch) action);break;
			case "UserWantToViewProfile" : UserWantToViewProfile((UserWantToViewProfile) action); break;
			case "UserWantToEditProfile" : UserWantToEditProfile((UserWantToEditProfile) action); break;
			case "UserWantsLeaderboard" : UserWantsLeaderboard((UserWantsLeaderboard) action); break;
		}

	}

	/**
	 * This function converts from messages the server got and handled to a format
	 * the user can expect to receive.
	 * @param msg
	 * @return String json message
	 */
	private String convertToCorrectJson(Message msg) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String msgJson = mapper.writeValueAsString(msg);

			JsonNode jsonNode = mapper.readTree(msgJson);
			String action = jsonNode.get("action").asText();

			switch (action) {
				case "LoginResponse" :{
					LoginResponse message = new LoginResponse("LoginResponse");
					message.setLoginStatus(( (LoginResponse) msg) .isLoginStatus());
					message.setResponse(((LoginResponse) msg).getResponse());
					message.setUserid(((LoginResponse) msg).getUserid());
					message.setDisplayname(((LoginResponse)msg).getDisplayname());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "RegisterResponse": {
					RegisterResponse message = new RegisterResponse("RegisterResponse");
					message.setRegisterStatus(( (RegisterResponse) msg) .isRegisterStatus());
					message.setResponse(((RegisterResponse) msg).getResponse());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "DiceThrowResponse" : {
					DiceThrowResponse message = new DiceThrowResponse("DiceThrowResponse");
					message.setDicerolled(((DiceThrowResponse)msg).getDicerolled());
					message.setGameid(((DiceThrowResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserHasConnectedResponse" : {
					UserHasConnectedResponse message = new UserHasConnectedResponse("UserHasConnectedResponse");
					message.setUserid(((UserHasConnectedResponse)msg).getUserid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "ChatJoinNewUserResponse" : {
					ChatJoinNewUserResponse message = new ChatJoinNewUserResponse("ChatJoinNewUserResponse");
					message.setDisplayname(((ChatJoinNewUserResponse)msg).getDisplayname());
					message.setChatroomname(((ChatJoinNewUserResponse)msg).getChatroomname());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "ChatJoinResponse" : {
					ChatJoinResponse message = new ChatJoinResponse("ChatJoinResponse");
					message.setStatus(((ChatJoinResponse)msg).isStatus());
					message.setResponse(((ChatJoinResponse)msg).getResponse());
					message.setChatroomname(((ChatJoinResponse)msg).getChatroomname());
					message.setUsersinroom(((ChatJoinResponse)msg).getUsersinroom());
					message.setChatlog(((ChatJoinResponse)msg).getChatlog());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "SentMessageResponse": {
					SentMessageResponse message = new SentMessageResponse("SentMessageResponse");
					message.setdisplayname(((SentMessageResponse)msg).getdisplayname());
					message.setChatroomname(((SentMessageResponse)msg).getChatroomname());
					message.setChatmessage(((SentMessageResponse)msg).getChatmessage());
					message.setTimestamp(((SentMessageResponse)msg).getTimestamp());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserLeftChatRoomResponse": {
					UserLeftChatRoomResponse message = new UserLeftChatRoomResponse("UserLeftChatRoomResponse");
					message.setDisplayname(((UserLeftChatRoomResponse)msg).getDisplayname());
					message.setChatroomname(((UserLeftChatRoomResponse)msg).getChatroomname());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "ErrorMessageResponse" : {
					ErrorMessageResponse message = new ErrorMessageResponse("ErrorMessageResponse");
					message.setMessage(((ErrorMessageResponse)msg).getMessage());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "ChatRoomsListResponse" : {
					ChatRoomsListResponse message = new ChatRoomsListResponse("ChatRoomsListResponse");
					message.setChatRoom(((ChatRoomsListResponse)msg).getChatRoom());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
                case "UsersListResponse" : {
                    UsersListResponse message = new UsersListResponse("UsersListResponse");
                    message.setDisplaynames(((UsersListResponse)msg).getDisplaynames());
                    String retString = mapper.writeValueAsString(message);
                    return retString;
                }
				case "CreateGameResponse": {
					CreateGameResponse message = new CreateGameResponse("CreateGameResponse");
					message.setGameid(((CreateGameResponse)msg).getGameid());
					message.setJoinstatus(((CreateGameResponse)msg).isJoinstatus());
					message.setResponse(((CreateGameResponse)msg).getResponse());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "SendGameInvitationsResponse": {
					SendGameInvitationsResponse message = new SendGameInvitationsResponse("SendGameInvitationsResponse");
					message.setHostdisplayname(((SendGameInvitationsResponse)msg).getHostdisplayname());
					message.setGameid(((SendGameInvitationsResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserJoinedGameResponse": {
					UserJoinedGameResponse message = new UserJoinedGameResponse("UserJoinedGameResponse");
					message.setPlayersinlobby(((UserJoinedGameResponse)msg).getPlayersinlobby());
					message.setUserid(((UserJoinedGameResponse)msg).getUserid());
					message.setGameid(((UserJoinedGameResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserDeclinedGameInvitationResponse":{
					UserDeclinedGameInvitationResponse message = new UserDeclinedGameInvitationResponse("UserDeclinedGameInvitationResponse");
					message.setUserid(((UserDeclinedGameInvitationResponse)msg).getUserid());
					message.setGameid(((UserDeclinedGameInvitationResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserLeftGameResponse":{
					UserLeftGameResponse message = new UserLeftGameResponse("UserLeftGameResponse");
					message.setGameid(((UserLeftGameResponse)msg).getGameid());
					message.setDisplayname(((UserLeftGameResponse)msg).getDisplayname());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "PieceMovedResponse": {
					PieceMovedResponse message = new PieceMovedResponse("PieceMovedResponse");
					message.setPlayerid(((PieceMovedResponse)msg).getPlayerid());
					message.setPiecemoved(((PieceMovedResponse)msg).getPiecemoved());
					message.setMovedto(((PieceMovedResponse)msg).getMovedto());
					message.setMovedfrom(((PieceMovedResponse)msg).getMovedfrom());
					message.setGameid(((PieceMovedResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "PlayerWonGameResponse":{
					PlayerWonGameResponse message = new PlayerWonGameResponse("PlayerWonGameResponse");
					message.setPlayerwonid(((PlayerWonGameResponse)msg).getPlayerwonid());
					message.setGameid(((PlayerWonGameResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "PlayerStateChangeResponse":{
					PlayerStateChangeResponse message = new PlayerStateChangeResponse("PlayerStateChangeResponse");
					message.setPlayerstate(((PlayerStateChangeResponse)msg).getPlayerstate());
					message.setActiveplayerid(((PlayerStateChangeResponse)msg).getActiveplayerid());
					message.setGameid(((PlayerStateChangeResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "GameHasStartedResponse":{
					GameHasStartedResponse message = new GameHasStartedResponse("GameHasStartedResponse");
					message.setGameid(((GameHasStartedResponse)msg).getGameid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserWantToViewProfileResponse":{
					UserWantToViewProfileResponse message = new UserWantToViewProfileResponse("UserWantToViewProfileResponse");
					message.setUserId(((UserWantToViewProfileResponse)msg).getUserId());
					message.setGamesWon(((UserWantToViewProfileResponse)msg).getGamesWon());
					message.setGamesPlayed(((UserWantToViewProfileResponse)msg).getGamesPlayed());
					message.setDisplayName(((UserWantToViewProfileResponse)msg).getDisplayName());
					message.setImageString(((UserWantToViewProfileResponse)msg).getImageString());
					message.setMessage(((UserWantToViewProfileResponse)msg).getMessage());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserWantToEditProfileResponse":{
					UserWantToEditProfileResponse message = new UserWantToEditProfileResponse("UserWantToEditProfileResponse");
					message.setResponse(((UserWantToEditProfileResponse)msg).getResponse());
					message.setChanged(((UserWantToEditProfileResponse)msg).isChanged());
					message.setDisplayname(((UserWantToEditProfileResponse)msg).getDisplayname());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "LeaderboardResponse":{
					LeaderboardResponse message = new LeaderboardResponse("LeaderboardResponse");
					message.setToptenplays(((LeaderboardResponse)msg).getToptenplays());
					message.setToptenwins(((LeaderboardResponse)msg).getToptenwins());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}

				default: {
					System.out.println(msg);
					return "{\"ERROR\":\"something went wrong\"}";
				}
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * When the user logs in without using the remember me function.
	 * @param action
	 */
	private void UserDoesLoginManual(ClientLogin action){

		LoginResponse retMsg = new LoginResponse("LoginResponse");
		retMsg.setRecipientSessionId(action.getRecipientSessionId());
		try {
			boolean status = db.checkIfLoginValid(action.getUsername(), action.getPassword());
			retMsg.setLoginStatus(status);

			if(retMsg.isLoginStatus()){ //If login was successful we set the userid on the client.
				retMsg.setResponse("OK");

				String userid = db.getUserId(action.getUsername());
				retMsg.setUserid(userid);
				setUseridToClient(action.getRecipientSessionId(), userid);
				UserInfo info = db.getProfile(userid);
				retMsg.setDisplayname(info.getDisplayName());

				int tokenCount = db.countSessionToken(userid);

				if (tokenCount > 0) { //Terminate existing token before inserting the new one.
					db.terminateSessionToken(userid);
					db.insertSessionToken(action.getRecipientSessionId(), userid);
				} else {
					db.insertSessionToken(action.getRecipientSessionId(), userid);
				}

				//Announce to users that the client has connected.
				AnnounceUserLoggedOn(action);

			} else {
				retMsg.setResponse("Username and/or password are incorrect");
			}

		} catch (SQLException e) {
			retMsg.setResponse("Internal Server Error");
			retMsg.setLoginStatus(false);
			e.printStackTrace();
		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	/**
	 * When a user logs in automatically using a remember me function.
	 * @param action
	 */
	private void UserDoesLoginAuto(ClientLogin action){

		LoginResponse retMsg = new LoginResponse("LoginResponse");
		retMsg.setRecipientSessionId(action.getRecipientSessionId());

		try {
			boolean status = db.checkIfLoginValid(action.getRecipientSessionId());
			retMsg.setLoginStatus(status);
			if(status) {
				retMsg.setResponse("Login was successful");
				String userid = db.getUserIdBySession(retMsg.getRecipientSessionId());

				retMsg.setUserid(userid);
				setUseridToClient(action.getRecipientSessionId(), userid);
				UserInfo info = db.getProfile(userid);
				retMsg.setDisplayname(info.getDisplayName());

			} else {
				retMsg.setResponse("Session token are invalid. Try again");
			}

		} catch (SQLException e) {
			retMsg.setLoginStatus(false);
			retMsg.setResponse("Internal server error");
			e.printStackTrace();
		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

		if (retMsg.isLoginStatus()){
			AnnounceUserLoggedOn(action);
		}
	}

	private void UserListChatrooms(UserListChatrooms action) {
		ChatRoomsListResponse retMsg = new ChatRoomsListResponse("ChatRoomsListResponse");
		retMsg.setRecipientSessionId(action.getRecipientSessionId());

		ArrayList<String> roomNames = new ArrayList<String>();
		for(ChatRoom room : activeChatRooms) {
			if (!room.isGameRoom()) {
				roomNames.add(room.getName());
			}
		}

		String[] arr = new String[roomNames.size()];
		for(int i = 0; i < roomNames.size(); i++) {
			arr[i] = roomNames.get(i);
		}

		retMsg.setChatRoom(arr);
		System.out.println(arr.toString());

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	/**
	 * When a new user logs into to the game server we announce their presence to all other clients.
	 * @param action
	 */
	private void AnnounceUserLoggedOn(Message action){
		Iterator<Client> iterator = clients.iterator();
		while(iterator.hasNext()){
			Client c = iterator.next();

			Message retMsg = new UserHasConnectedResponse("UserHasConnectedResponse");
			retMsg.setRecipientSessionId(c.getUuid());
			((UserHasConnectedResponse) retMsg).setUserid(( sessionIdToUserId(action.getRecipientSessionId())));

			//No need to announce to the originator of the message.
			if (!c.getUuid().contentEquals(retMsg.getRecipientSessionId())){
				synchronized (messagesToSend) {
					messagesToSend.add(retMsg);
				}
			}
		}
	}

	/**
	 * Logic for when a client want to register an account.
	 * @param action
	 */
	private void UserDoesRegister(ClientRegister action){

		Message retMsg = new RegisterResponse("RegisterResponse");
		retMsg.setRecipientSessionId(action.getRecipientSessionId());

		/* Check if the username already exists */

		try {
			boolean usernameExists = db.doesAccountNameExist(action.getUsername());
			if (!usernameExists) {
				db.insertAccount(action.getUsername(), action.getPassword());
				((RegisterResponse)retMsg).setRegisterStatus(true);
				((RegisterResponse)retMsg).setResponse("Registration successful");
			} else {
				((RegisterResponse)retMsg).setRegisterStatus(false);
				((RegisterResponse)retMsg).setResponse("User with username " + action.getUsername() + " already exists");
			}

		} catch (SQLException e) {
			((RegisterResponse)retMsg).setRegisterStatus(false);
			((RegisterResponse)retMsg).setResponse("Internal server error");
			e.printStackTrace();
		}
		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}


	private void UserJoinChat(UserJoinChat action) {
		Message retMsg = new ChatJoinResponse("ChatJoinResponse");
		retMsg.setRecipientSessionId(useridToSessionId(action.getUserid()));

		if (chatRoomExists(action.getChatroomname())) {

			if (chatRoomIsGameOnly(action.getChatroomname())) {
				UserInfo info = db.getProfile(action.getUserid());
				if (!userIsAllowedInRoom(action.getChatroomname(), info.getDisplayName())){
					((ChatJoinResponse)retMsg).setResponse("You are not allowed to join this room");
					((ChatJoinResponse)retMsg).setChatroomname(action.getChatroomname());
					((ChatJoinResponse)retMsg).setStatus(false);
					synchronized (messagesToSend) {
						messagesToSend.add(retMsg);
						return; //We dont do anything else here.
					}
				}
			}

			boolean added = addUserToChatroom(action.getChatroomname(), action.getUserid());

			((ChatJoinResponse)retMsg).setStatus(added);
			((ChatJoinResponse)retMsg).setChatroomname(action.getChatroomname());

			if (added) {
				((ChatJoinResponse)retMsg).setResponse("Joined room successfully");

				((ChatJoinResponse)retMsg).setUsersinroom(getUsersInChatRoom(action.getChatroomname()));
				((ChatJoinResponse)retMsg).setChatlog(getChatLog(action.getChatroomname()));

				//Announce the users presence to others in the chat room.
				announceToUsersInChatRoom(retMsg, action.getChatroomname());

			} else {
				((ChatJoinResponse)retMsg).setResponse("Attempt to join room was unsuccessful");
			}

		} else { //Create chatroom and join it.

			try {
				db.insertChatRoom(action.getChatroomname());
				boolean created = db.isChatRoom(action.getChatroomname());
				if (created) {
					ChatRoom room = new ChatRoom(action.getChatroomname());
					room.getConnectedUsers().add(action.getUserid());
					activeChatRooms.add(room);

					((ChatJoinResponse)retMsg).setStatus(true);
					((ChatJoinResponse)retMsg).setResponse("Room created and joined successfully");
					((ChatJoinResponse)retMsg).setChatroomname(action.getChatroomname());

					//Announce the users presence to others in the chat room.
					announceToUsersInChatRoom(retMsg, action.getChatroomname());

					((ChatJoinResponse)retMsg).setUsersinroom(getUsersInChatRoom(action.getChatroomname()));
					((ChatJoinResponse)retMsg).setChatlog(getChatLog(action.getChatroomname()));

				} else {
					((ChatJoinResponse)retMsg).setStatus(false);
					((ChatJoinResponse)retMsg).setResponse("Creating room failed. Try again");
					((ChatJoinResponse)retMsg).setChatroomname(action.getChatroomname());
				}

			} catch (SQLException e) {
				e.printStackTrace();
				((ChatJoinResponse)retMsg).setStatus(false);
				((ChatJoinResponse)retMsg).setResponse("Internal server error");
				((ChatJoinResponse)retMsg).setChatroomname(action.getChatroomname());
			}

		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	private ChatMessage[] getChatLog(String chatroomname){
		ArrayList<ChatMessage> arraylist = db.getChatMessages(chatroomname);
		ChatMessage[] arr;

		if  (arraylist.size() >= 50) {
			List<ChatMessage> subArraylist = arraylist.subList(arraylist.size()-50, arraylist.size());
			arr = new ChatMessage[subArraylist.size() +1];
			for (int i = 0; i <= subArraylist.size(); i++){
				arr[i] = arraylist.get(i);
			}
		} else {
			arr = new ChatMessage[arraylist.size()];
			for (int i = 0; i < arraylist.size(); i++){
				arr[i] = arraylist.get(i);
			}
		}

		return arr;
	}

	private void UserLeftChatRoom(UserLeftChatRoom action){
		Message retMsg;
		String recipientId = useridToSessionId(action.getUserid());
		System.out.println(recipientId);

		if (userIsInChatroom(action.getChatroomname(),action.getUserid())) {
			boolean removed = removeUserFromChatroom(action.getChatroomname(), action.getUserid());

			if (removed) {
				retMsg = new UserLeftChatRoomResponse("UserLeftChatRoomResponse");
				retMsg.setRecipientSessionId(recipientId);
				((UserLeftChatRoomResponse)retMsg).setChatroomname(action.getChatroomname());

				UserInfo info = db.getProfile(action.getUserid());

				((UserLeftChatRoomResponse)retMsg).setDisplayname(info.getDisplayName());

				announceRemovalToUsersInChatRoom(info, action.getChatroomname());
			} else {
				retMsg = new ErrorMessageResponse("ErrorMessageResponse");
				retMsg.setRecipientSessionId(recipientId);
				((ErrorMessageResponse)retMsg).setMessage("Could not remove you from the chat room");
			}

		} else {
			retMsg = new ErrorMessageResponse("ErrorMessageResponse");
			retMsg.setRecipientSessionId(recipientId);
			((ErrorMessageResponse)retMsg).setMessage("You are not in the requested chat room");
		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	/**
	 * When a user wants to send a message to a chat room.
	 * @param action
	 */
	private void UserSentMessage(UserSentMessage action) {
		Message retMsg = new SentMessageResponse("SentMessageResponse");

		boolean isConnected, roomExists;

		isConnected = userIsInChatroom(action.getChatroomname(), action.getUserid());
		roomExists = chatRoomExists(action.getChatroomname());

		if (roomExists && isConnected){
			try {
				db.insertChatMessage(action.getChatroomname(), action.getUserid(), action.getChatmessage());
				UserInfo info = db.getProfile(action.getUserid());
				((SentMessageResponse)retMsg).setdisplayname(info.getDisplayName());
				((SentMessageResponse)retMsg).setChatroomname(action.getChatroomname());
				((SentMessageResponse)retMsg).setChatmessage(action.getChatmessage());
				((SentMessageResponse)retMsg).setTimestamp(String.valueOf(Instant.now().getEpochSecond()));

				sendMessageToChatRoom(retMsg, ((SentMessageResponse) retMsg).getChatroomname());

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else { //If the chatroom is non existent give the user a error message.
			retMsg = new ErrorMessageResponse("ErrorMessageResponse");
			if (!isConnected) {
				((ErrorMessageResponse)retMsg).setMessage("You are not connected to this chat room");
			}

			if (!roomExists) {
				((ErrorMessageResponse)retMsg).setMessage("No chatroom named " + action.getChatroomname() + " exists");
			}

			retMsg.setRecipientSessionId(useridToSessionId(action.getUserid()));

			synchronized (messagesToSend) {
				messagesToSend.add(retMsg);
			}
		}


	}

	private void UserDoesDiceThrow(UserDoesDiceThrow action){
		for(Ludo game : activeLudoGames) {
			if (game.getGameid().contentEquals(action.getGameid())) {
				game.throwDice();
			}
		}
	}

	private void UserDoesPieceMove(UserDoesPieceMove action){
		for (Ludo game : activeLudoGames) {
			if (game.getGameid().contentEquals(action.getGameid())){
				game.movePiece(action.getPlayerid(), action.getMovedfrom(), action.getMovedto());
			}
		}
	}

	/**
	 * Converts session UUID to userid
	 * @param sessionId
	 * @return userid
	 */
	private String sessionIdToUserId(String sessionId){
		Iterator<Client> c = clients.iterator();
		while(c.hasNext()) {
			Client client = c.next();
			if (client.getUuid().contentEquals(sessionId)) {
				return client.getUserId();
			}
		}

		return null;
	}

	/**
	 * Converts userid to sessionid
	 * @param userid
	 * @return sessionid
	 */
	private String useridToSessionId(String userid){
		Iterator<Client> c = clients.iterator();
		while(c.hasNext()) {
			Client client = c.next();
			if (client.getUserId().contentEquals(userid)) {
				return client.getUuid();
			}
		}

		return null;
	}

	/**
	 * Sets userid to client
	 * @param sessionId
	 * @param userid
	 */
	private void setUseridToClient(String sessionId , String userid){
		Iterator<Client> c = clients.iterator();
		while(c.hasNext()) {
			Client client = c.next();
			if (client.getUuid().contentEquals(sessionId)) {
				client.setUserId(userid);
				return;
			}
		}
	}

	/**
	 * Adds a user to the chatroom
	 * @param chatRoomName
	 * @param userid
	 * @return
	 */
	private boolean addUserToChatroom(String chatRoomName, String userid){
		for(ChatRoom room : activeChatRooms) {
			if (room.getName().toLowerCase().contentEquals(chatRoomName.toLowerCase())) {
				if (!room.connectedUsers.contains(userid)) {
					room.connectedUsers.add(userid);
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private boolean userIsInChatroom(String chatRoomName, String userid) {
		for(ChatRoom room : activeChatRooms) {
			if (room.getName().toLowerCase().contentEquals(chatRoomName.toLowerCase())) {
				if (room.connectedUsers.contains(userid)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private String[] getUsersInChatRoom(String chatRoomName){
		String[] arr = new String[0];
		ArrayList<String> arrayList = null;

		for(ChatRoom room : activeChatRooms) {
			if (room.getName().toLowerCase().contentEquals(chatRoomName.toLowerCase())){
				arrayList = new ArrayList<>();
				for (String userid : room.getConnectedUsers()){
					UserInfo info = db.getProfile(userid);
					arrayList.add(info.getDisplayName());
				}
			}
		}

		if (arrayList.size() > 0) {
			arr = new String[arrayList.size()];
			for ( int i = 0; i < arrayList.size(); i++) {
				arr[i] = arrayList.get(i);
			}
		}

		return arr;
	}

	/**
	 * Send chat message to a chat room
	 * @param action Message object
	 */
	private void sendMessageToChatRoom(Message action, String chatroom){

		for (ChatRoom room : activeChatRooms) { //Loop over chat rooms
			if (room.getName().contentEquals(chatroom)){ // Find correct chat room
				for(String UserId : room.getConnectedUsers()){ //Get all active users
					Message sentMessageResponse = new SentMessageResponse("SentMessageResponse");
					((SentMessageResponse)sentMessageResponse).setChatmessage(((SentMessageResponse)action).getChatmessage());
					((SentMessageResponse)sentMessageResponse).setChatroomname(chatroom);
					((SentMessageResponse)sentMessageResponse).setdisplayname(((SentMessageResponse)action).getdisplayname());
					((SentMessageResponse)sentMessageResponse).setTimestamp(((SentMessageResponse)action).getTimestamp());
					sentMessageResponse.setRecipientSessionId(useridToSessionId(UserId));

					synchronized (messagesToSend) {
						messagesToSend.add(sentMessageResponse); //Send message.
					}
				}
				return;
			}
		}

	}

	/**
	 * Announces that a user has entered their chat room
	 * @param action
	 * @param chatroomname
	 */
	private void announceToUsersInChatRoom(Message action, String chatroomname){

		UserInfo info = db.getProfile(sessionIdToUserId(action.getRecipientSessionId()));

		for (ChatRoom room : activeChatRooms) { //Loop over chat rooms
			if (room.getName().contentEquals(chatroomname)){ // Find correct chat room
				for(String UserId : room.getConnectedUsers()){ //Get all active users
					if (!UserId.contentEquals(sessionIdToUserId(action.getRecipientSessionId()))) {
						Message chatJoinNewUserResponse = new ChatJoinNewUserResponse("ChatJoinNewUserResponse");
						((ChatJoinNewUserResponse)chatJoinNewUserResponse).setDisplayname(info.getDisplayName());
						((ChatJoinNewUserResponse)chatJoinNewUserResponse).setChatroomname(room.getName());
						chatJoinNewUserResponse.setRecipientSessionId(useridToSessionId(UserId));

						synchronized (messagesToSend) {
							messagesToSend.add(chatJoinNewUserResponse); //Send message.
						}
					}
				}
				return;
			}
		}
	}

	/**
	 * Removes a user from a chatroom
	 * @param chatRoomName
	 * @param userid
	 * @return
	 */
	private boolean removeUserFromChatroom(String chatRoomName, String userid) {
		for(ChatRoom room : activeChatRooms) {
			if (room.getName().toLowerCase().contentEquals(chatRoomName.toLowerCase())) {
				if (room.connectedUsers.contains(userid)) {
					room.connectedUsers.remove(userid);

					if (room.getConnectedUsers().size() == 0 && !room.getName().toLowerCase().contentEquals("global") ) { //Delete it
						activeChatRooms.remove(room);
						try {
							db.removeChatRoom(room.getName()); //Remove from DB.
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}

					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Announce the removal of a user in a chat room to other users in the same chat room
	 * @param info
	 * @param chatroomname
	 */
	private void announceRemovalToUsersInChatRoom(UserInfo info, String chatroomname){

		for (ChatRoom room : activeChatRooms) { //Loop over chat rooms
			if (room.getName().contentEquals(chatroomname)){ // Find correct chat room
				for(String UserId : room.getConnectedUsers()){ //Get all active users
						Message userLeftChatRoomResponse = new UserLeftChatRoomResponse("UserLeftChatRoomResponse");
						((UserLeftChatRoomResponse)userLeftChatRoomResponse).setDisplayname(info.getDisplayName());
						((UserLeftChatRoomResponse)userLeftChatRoomResponse).setChatroomname(chatroomname);
						userLeftChatRoomResponse.setRecipientSessionId(useridToSessionId(UserId));

						synchronized (messagesToSend) {
							messagesToSend.add(userLeftChatRoomResponse); //Send message.
						}
					}
				return;
			}
		}
	}

	private boolean chatRoomExists(String chatRoomName){
		for(ChatRoom room: activeChatRooms) {
			if (room.getName().toLowerCase().contentEquals(chatRoomName.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private boolean chatRoomIsGameOnly(String chatRoomName){
		for(ChatRoom room: activeChatRooms) {
			if (room.getName().toLowerCase().contentEquals(chatRoomName.toLowerCase())) {
				return room.isGameRoom();
			}
		}
		return false;
	}

	private boolean userIsAllowedInRoom(String chatRoomName, String displayname) {
		for(ChatRoom room: activeChatRooms) {
			if (room.getName().toLowerCase().contentEquals(chatRoomName.toLowerCase())) {
				for(String name: room.getAllowedUsers()){
					if (name.contentEquals(displayname)){
						return true;
					}
				}
			}
		}
		return false;
	}

    /**
     * Finds all names matching the search query.
     * @param action
     */
	private void UserWantsUsersList(UserWantsUsersList action){

	    Message retMsg = new UsersListResponse("UsersListResponse");
	    retMsg.setRecipientSessionId(action.getRecipientSessionId());
	    UserInfo info_self = db.getProfile(sessionIdToUserId(retMsg.getRecipientSessionId()));

	    ArrayList<String> usersMatchQuery = new ArrayList<>();

        for (ChatRoom room : activeChatRooms) {
            for (String userid : room.getConnectedUsers()){
                UserInfo info = db.getProfile(userid);
                if (info.getDisplayName().contains(action.getSearchquery()) && !info.getDisplayName().contentEquals(info_self.getDisplayName())) {
                    if (!usersMatchQuery.contains(info.getDisplayName())){
                        usersMatchQuery.add(info.getDisplayName());
                    }
                }
            }
        }

        String[] retArr = new String[usersMatchQuery.size()];
        for (int i = 0; i < usersMatchQuery.size(); i++) {
            retArr[i] = usersMatchQuery.get(i);
        }

        ((UsersListResponse)retMsg).setDisplaynames(retArr);

        synchronized (messagesToSend) {
            messagesToSend.add(retMsg);
        }

    }

    private void UserWantsToCreateGame(UserWantsToCreateGame action){
		Message retMsg = new CreateGameResponse("CreateGameResponse");
		retMsg.setRecipientSessionId(useridToSessionId(action.getHostid()));

		Ludo newGame = new Ludo();
		newGame.setHostid(action.getHostid());
		newGame.setGameid(UUID.randomUUID().toString());

		UserInfo info = db.getProfile(action.getHostid());
		newGame.addPlayer(info.getDisplayName());
		newGame.addDiceListener(this);
		newGame.addPieceListener(this);
		newGame.addPlayerListener(this);
		activeLudoGames.add(newGame);

		((CreateGameResponse)retMsg).setJoinstatus(true);
		((CreateGameResponse)retMsg).setResponse("Joined game successfully");
		((CreateGameResponse)retMsg).setGameid(newGame.getGameid());

		System.out.println("Active ludo games " + activeLudoGames.size());

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

		Invitations invites = new Invitations();
		invites.setPlayers(action.getToinvitedisplaynames());
		invites.setAccepted(new Boolean[action.getToinvitedisplaynames().length]);
		invites.setGameid(newGame.getGameid());
		pendingInvites.add(invites);

		//Send out invitations here:
		for (int i = 0; i < action.getToinvitedisplaynames().length; i++) {
			SendGameInvitationsResponse invite = new SendGameInvitationsResponse("SendGameInvitationsResponse");
			UserInfo userInfo = db.getProfilebyDisplayName(action.getToinvitedisplaynames()[i]);
			if (userInfo != null){
				invite.setGameid(newGame.getGameid());
				invite.setHostdisplayname(info.getDisplayName());
				invite.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));
				synchronized (messagesToSend){
					messagesToSend.add(invite);
				}
			}
		}

		//Create a game room.
		ChatRoom newRoom = new ChatRoom(newGame.getGameid());
		newRoom.setGameRoom(true);
		ArrayList<String> names = new ArrayList<>();
		for (String name: action.getToinvitedisplaynames()) {
			names.add(name);
		}
		newRoom.setAllowedUsers(names);
		activeChatRooms.add(newRoom);
	}

	private void UserDoesGameInvitationAnswer(UserDoesGameInvitationAnswer action) {
		Message retMsg;
		System.out.println(action);
		if (action.isAccepted()) { //User accepted. Add them to the game
			retMsg = new UserJoinedGameResponse("UserJoinedGameResponse");
			((UserJoinedGameResponse)retMsg).setGameid(action.getGameid());
			((UserJoinedGameResponse)retMsg).setUserid(action.getUserid());
			for(Ludo game : activeLudoGames) {
				System.out.println(game.getGameid() + " " + action.getGameid());
				if (game.getGameid().contentEquals(action.getGameid())) {
					UserInfo info = db.getProfile(action.getUserid());
					game.addPlayer(info.getDisplayName());
					((UserJoinedGameResponse)retMsg).setPlayersinlobby(game.getPlayers());
				}
			}

			for(Ludo game : activeLudoGames) {
				if (game.getGameid().contentEquals(action.getGameid())) {
					for (String name : game.getPlayers()) {
						if (name != null){
							UserInfo info = db.getProfilebyDisplayName(name);
							retMsg.setRecipientSessionId(useridToSessionId(info.getUserId()));
							synchronized (messagesToSend){
								messagesToSend.add(retMsg);
							}
						}
					}
				}
			}

			for (Invitations invite : pendingInvites) {
				if (invite.getGameid().contentEquals(action.getGameid())){
					UserInfo info = db.getProfile(action.getUserid());
					invite.setOneUpdate(info.getDisplayName(),true);
				}
			}

		} else { //User declined. Send message to inviter. Which is host of game (?)
			String hostId = null;
			for(Ludo game : activeLudoGames) {
				if (game.getGameid().contentEquals(action.getGameid())) {
					hostId = game.getHostid();
				}
			}
			retMsg = new UserDeclinedGameInvitationResponse("UserDeclinedGameInvitationResponse");
			retMsg.setRecipientSessionId(useridToSessionId(hostId));
			((UserDeclinedGameInvitationResponse)retMsg).setGameid(action.getGameid());
			((UserDeclinedGameInvitationResponse)retMsg).setUserid(action.getUserid());

			synchronized (messagesToSend){
				messagesToSend.add(retMsg);
			}

			for (Invitations invite : pendingInvites) {
				if (invite.getGameid().contentEquals(action.getGameid())){
					UserInfo info = db.getProfile(action.getUserid());
					invite.setOneUpdate(info.getDisplayName(),false);
				}
			}

		}
		checkIfEveryoneAnsweredInvite(action);
	}

	private void checkIfEveryoneAnsweredInvite(UserDoesGameInvitationAnswer action){
		for (Invitations invite : pendingInvites) {
			if (invite.getGameid().contentEquals(action.getGameid())){
				UserInfo info = db.getProfile(action.getUserid());

				if (invite.isEveryoneAccepted()){
					for (int i = 0; i < invite.getPlayers().length; i++){
						if (invite.getOnePlayerAccepted(i)){
							Message gameStarted = new GameHasStartedResponse("GameHasStartedResponse");
							((GameHasStartedResponse)gameStarted).setGameid(invite.getGameid());
							UserInfo userInfo = db.getProfilebyDisplayName(invite.getOnePlayerName(i));
							gameStarted.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));

							synchronized (messagesToSend){
								messagesToSend.add(gameStarted);
							}

						}
					}
					//pendingInvites.remove(invite);
					for (Ludo game : activeLudoGames) {
						Message gameStarted = new GameHasStartedResponse("GameHasStartedResponse");
						((GameHasStartedResponse)gameStarted).setGameid(invite.getGameid());
						gameStarted.setRecipientSessionId(useridToSessionId(game.getHostid()));
						messagesToSend.add(gameStarted);
					}
				}

			}
		}
	}

	private void UserLeftGame(UserLeftGame action){

		UserLeftGameResponse retMsg = new UserLeftGameResponse("UserLeftGameResponse");
		UserInfo info = db.getProfile(sessionIdToUserId(action.getRecipientSessionId()));
		retMsg.setDisplayname(info.getDisplayName());
		retMsg.setGameid(action.getGameid());

		for (Ludo game : activeLudoGames) {
			if (game.getGameid().contentEquals(action.getGameid())) {
				game.removePlayer(info.getDisplayName());
				for(String name : game.getActivePlayers()){
					if (!name.contentEquals(info.getDisplayName())){
						UserInfo userInfo = db.getProfilebyDisplayName(name);
						retMsg.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));
						System.out.println(name);
						synchronized (messagesToSend){
							messagesToSend.add(retMsg);
						}
					}
				}
			}
		}
	}

	private void UserDoesRandomGameSearch(UserDoesRandomGameSearch action){
		UserInfo info = db.getProfile(action.getUserid());

		boolean foundGame = false;

		for (Ludo game : activeLudoGames) {
			System.out.println("Status: " + game.getStatus());
			if (game.getStatus().contentEquals("Initiated") && game.getActivePlayers().length < 4){
				foundGame = true;
				game.addPlayer(info.getDisplayName());

				Message retMsg = new UserJoinedGameResponse("UserJoinedGameResponse");
				((UserJoinedGameResponse)retMsg).setGameid(game.getGameid());
				((UserJoinedGameResponse)retMsg).setUserid(action.getUserid());
				((UserJoinedGameResponse)retMsg).setPlayersinlobby(game.getPlayers());

				for (String name : game.getPlayers()) {
					UserInfo userInfo = db.getProfilebyDisplayName(name);
					retMsg.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));
					synchronized (messagesToSend){
						messagesToSend.add(retMsg);
					}
				}

				for(ChatRoom room : activeChatRooms) {
					if (room.getName().contentEquals(game.getGameid())){
						ArrayList<String> names = room.getAllowedUsers();
						names.add(info.getDisplayName());
						room.setAllowedUsers(names);
					}
				}

				if(game.getActivePlayers().length == 4) {
					for (String name : game.getPlayers()) {
						Message gameStarted = new GameHasStartedResponse("GameHasStartedResponse");
						((GameHasStartedResponse)gameStarted).setGameid(game.getGameid());
						UserInfo userInfo = db.getProfilebyDisplayName(name);
						gameStarted.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));
						synchronized (messagesToSend){
							messagesToSend.add(gameStarted);
						}
					}
				}

			}
		}

		if (!foundGame) {
			Message retMsg = new CreateGameResponse("CreateGameResponse");
			retMsg.setRecipientSessionId(action.recipientSessionId);

			Ludo newGame = new Ludo();
			newGame.setHostid(sessionIdToUserId(action.getRecipientSessionId()));
			newGame.setGameid(UUID.randomUUID().toString());

			newGame.addPlayer(info.getDisplayName());
			newGame.addDiceListener(this);
			newGame.addPieceListener(this);
			newGame.addPlayerListener(this);
			activeLudoGames.add(newGame);

			((CreateGameResponse)retMsg).setJoinstatus(true);
			((CreateGameResponse)retMsg).setResponse("Joined game successfully");
			((CreateGameResponse)retMsg).setGameid(newGame.getGameid());
			synchronized (messagesToSend) {
				messagesToSend.add(retMsg);
			}

			//Create a game room.
			ChatRoom newRoom = new ChatRoom(newGame.getGameid());
			newRoom.setGameRoom(true);
			ArrayList<String> names = new ArrayList<>();
			names.add(info.getDisplayName());
			newRoom.setAllowedUsers(names);
			activeChatRooms.add(newRoom);

		}

	}

	private void UserWantToViewProfile(UserWantToViewProfile action){
		UserInfo info = db.getProfilebyDisplayName(action.getDisplayname());
		Message retMsg;
		retMsg = new UserWantToViewProfileResponse("UserWantToViewProfileResponse");
		retMsg.setRecipientSessionId(action.getRecipientSessionId());
		if (info != null){
			((UserWantToViewProfileResponse)retMsg).setImageString(info.getAvatarImage());
			((UserWantToViewProfileResponse)retMsg).setDisplayName(info.getDisplayName());
			((UserWantToViewProfileResponse)retMsg).setGamesPlayed(info.getGamesPlayed());
			((UserWantToViewProfileResponse)retMsg).setGamesWon(info.getGamesWon());
			((UserWantToViewProfileResponse)retMsg).setUserId(info.getUserId());
			((UserWantToViewProfileResponse)retMsg).setMessage("");

		} else {
			((UserWantToViewProfileResponse)retMsg).setMessage("No user with displayname " + action.getDisplayname());
		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	private void UserWantToEditProfile(UserWantToEditProfile action) {
		Message retMsg = new UserWantToEditProfileResponse("UserWantToEditProfileResponse");
		retMsg.setRecipientSessionId(action.getRecipientSessionId());
		UserInfo oldInfo = db.getProfile(sessionIdToUserId(action.getRecipientSessionId()));
		UserInfo newInfo = new UserInfo(sessionIdToUserId(action.getRecipientSessionId()), action.getDisplayname(),action.getImageString(), oldInfo.getGamesPlayed(), oldInfo.getGamesWon());

        boolean profileUpdate = false, passwordUpdate = false;

		try {
			db.updateProfile(newInfo);
			profileUpdate = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (!action.getPassword().isEmpty()){
			try {
				db.updateAccountPassword(sessionIdToUserId(action.getRecipientSessionId()), action.getPassword());
				passwordUpdate = true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else { //Make true anyways.
			passwordUpdate = true;
		}

		if (profileUpdate && passwordUpdate) { //Both updated.
			((UserWantToEditProfileResponse)retMsg).setChanged(true);
			((UserWantToEditProfileResponse)retMsg).setResponse("Updated the whole profile successfully.");
			((UserWantToEditProfileResponse)retMsg).setDisplayname(newInfo.getDisplayName());
            System.out.println(newInfo.toString());

        } else if (profileUpdate && !passwordUpdate) { //Only profile was updated
			((UserWantToEditProfileResponse)retMsg).setChanged(true);
			((UserWantToEditProfileResponse)retMsg).setResponse("Profile was updated successfully. Password update failed.");
			((UserWantToEditProfileResponse)retMsg).setDisplayname(action.getDisplayname());

		} else if (!profileUpdate && passwordUpdate) { //Only password was updated
			((UserWantToEditProfileResponse)retMsg).setChanged(true);
			((UserWantToEditProfileResponse)retMsg).setResponse("Profile was not updated. Password updated successfully.");
			((UserWantToEditProfileResponse)retMsg).setDisplayname(oldInfo.getDisplayName());

		} else { //Neither was updated.
			((UserWantToEditProfileResponse)retMsg).setChanged(false);
			((UserWantToEditProfileResponse)retMsg).setResponse("Something went wrong when updating user information");
			((UserWantToEditProfileResponse)retMsg).setDisplayname(oldInfo.getDisplayName());
		}

		synchronized (messagesToSend){
			messagesToSend.add(retMsg);
		}
	}

	private void UserWantsLeaderboard(UserWantsLeaderboard action) {
		Message retMsg = new LeaderboardResponse("LeaderboardResponse");
		retMsg.setRecipientSessionId(action.getRecipientSessionId());
		TopTenList toptenlist = db.getTopTenList();
		((LeaderboardResponse)retMsg).setToptenwins(toptenlist.getWonEntries());
		((LeaderboardResponse)retMsg).setToptenplays(toptenlist.getPlayedEntries());

		synchronized (messagesToSend){
			messagesToSend.add(retMsg);
		}
	}

	/**
	 * Implemented interface DiceListener
	 * @param diceEvent returns data about dice rolled
	 */
	@Override
	public void diceThrown(DiceEvent diceEvent) {
		Ludo game = diceEvent.getLudoGame();

		for (String name : game.getPlayers()){
			Message retMsg = new DiceThrowResponse("DiceThrowResponse");

			((DiceThrowResponse)retMsg).setGameid(game.getGameid());
			((DiceThrowResponse)retMsg).setDicerolled(diceEvent.getDiceRolled());
			UserInfo userInfo = db.getProfilebyDisplayName(name);
			retMsg.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));
			synchronized (messagesToSend){
				messagesToSend.add(retMsg);
			}
		}


	}

	/**
	 * Implemented interface PieceListener
	 * @param pieceEvent returns data about the piece moved
	 */
	@Override
	public void pieceMoved(PieceEvent pieceEvent) {
		Ludo game = pieceEvent.getLudoGame();
		for (String name : game.getPlayers()){

			Message retMsg = new PieceMovedResponse("PieceMovedResponse");

			((PieceMovedResponse)retMsg).setGameid(game.getGameid());
			((PieceMovedResponse)retMsg).setMovedfrom(pieceEvent.getFrom());
			((PieceMovedResponse)retMsg).setMovedto(pieceEvent.getTo());
			((PieceMovedResponse)retMsg).setPiecemoved(pieceEvent.getPieceMoved());
			((PieceMovedResponse)retMsg).setPlayerid(pieceEvent.getPlayerID());
			UserInfo userInfo = db.getProfilebyDisplayName(name);
			retMsg.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));
			synchronized (messagesToSend){
				messagesToSend.add(retMsg);
			}
		}

	}

	/**
	 * Implemented interface PlayerListener
	 * @param event PlayerEvent containing info about what state the player is in.
	 */
	@Override
	public void playerStateChanged(PlayerEvent event) {
		Message retMsg;
		Ludo game = event.getLudo();
		if(event.getPlayerEvent().contentEquals("Won")){
			int playerid = 0; //Represents which player we are looping through. This works since ludo game id
							  // Have the same order as player names.
			for (String name : game.getPlayers()){
				retMsg = new PlayerWonGameResponse("PlayerWonGameResponse");
				((PlayerWonGameResponse)retMsg).setPlayerwonid(event.getPlayerID());
				((PlayerWonGameResponse)retMsg).setGameid(game.getGameid());
				UserInfo info = db.getProfilebyDisplayName(name);
				info.setGamesPlayed(info.getGamesPlayed() + 1);

				//ONLY FOR WINNER.
				if(playerid == event.getPlayerID()) {
					info.setGamesWon(info.getGamesWon() + 1);
				}

				try {
					db.updateProfile(info);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				retMsg.setRecipientSessionId(useridToSessionId(info.getUserId()));
				synchronized (messagesToSend){
					messagesToSend.add(retMsg);
				}
			}

		} else {
			for (String name : game.getPlayers()){
				retMsg = new PlayerStateChangeResponse("PlayerStateChangeResponse");
				((PlayerStateChangeResponse)retMsg).setGameid(game.getGameid());
				((PlayerStateChangeResponse)retMsg).setActiveplayerid(event.getPlayerID());
				((PlayerStateChangeResponse)retMsg).setPlayerstate(event.getPlayerEvent());
				UserInfo userInfo = db.getProfilebyDisplayName(name);
				retMsg.setRecipientSessionId(useridToSessionId(userInfo.getUserId()));
				synchronized (messagesToSend){
					messagesToSend.add(retMsg);
				}
			}
		}


	}


}