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
	Database db = Database.getDatabase(); //Database singleton

	ArrayList<Ludo> activeLudoGames = new ArrayList<>(); //ArrayList over active games.
	ArrayList<ChatRoom> activeChatRooms = new ArrayList<>(); //ArrayList over chat rooms.

	LinkedList<Client> clients = new LinkedList<>(); //LinkedList containing clients

	boolean stopping = false; //Boolean to stop the server

	ArrayBlockingQueue<Message> objectsToHandle = new ArrayBlockingQueue<>(100); //Queue for incoming messages

	ArrayBlockingQueue<Message> messagesToSend = new ArrayBlockingQueue<>(100); //Queue for outbound messages

	ArrayBlockingQueue<Client> disconnectedClients = new ArrayBlockingQueue<>(1000); //Queue for clients that is to be disconnected.

	public static void main(String[] args) {
		new Server(); //Create a new server instance.
	}

	public Server(){
		startServerThread();
		startListener();
		startHandlingActions();
		startSenderThread();
		sendPingMessage();
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
									c.parseUsername(msg);
									System.out.println("Connected user : " + c.getUuid() + " " + msg);
									Message toBeQueued = parser.parseJson(msg);
									if (toBeQueued != null) { //Discard if it is null
										objectsToHandle.add(parser.parseJson(msg)); //Add the object to queue for handling
									} else {
										System.out.println("DISCARDED MESSAGE : " + msg);
									}
								}
							} else if (msg != null ) {
								synchronized (objectsToHandle) {
									Message toBeQueued = parser.parseJson(msg);
									if (toBeQueued != null) {
										objectsToHandle.add(parser.parseJson(msg)); //Add the object to queue for handling
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
						if (msg.getRecipientSessionId().contentEquals(c.getUuid())) {
							try {
								String converted = convertToCorrectJson(msg);
								System.out.println(converted);
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
		for(ChatRoom room : activeChatRooms){
			if(room.getConnectedUsers().contains(userId)){
				removeUserFromChatroom(room.getName(), userId);
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
			case "UserDoesDiceThrow": UserDoesDiceThrow(action);break;
			case "UserDoesLoginManual": UserDoesLoginManual((ClientLogin) action);break;
			case "UserDoesLoginAuto": UserDoesLoginAuto((ClientLogin) action); break;
			case "UserDoesRegister": UserDoesRegister((ClientRegister) action); break;
			case "UserJoinChat": UserJoinChat((UserJoinChat) action); break;
			case "UserSentMessage": UserSentMessage((UserSentMessage) action); break;
			case "UserLeftChatRoom": UserLeftChatRoom((UserLeftChatRoom) action); break;
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
				case "ServerThrowDice" : {
					//TODO : Not done.
					ServerThrowDice ret;
					ret = mapper.readValue(msgJson, ServerThrowDice.class);
					String retString = mapper.writeValueAsString(ret);
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
					message.setUserid(((ChatJoinNewUserResponse)msg).getUserid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "ChatJoinResponse" : {
					ChatJoinResponse message = new ChatJoinResponse("ChatJoinResponse");
					message.setStatus(((ChatJoinResponse)msg).isStatus());
					message.setResponse(((ChatJoinResponse)msg).getResponse());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "SentMessageResponse": {
					SentMessageResponse message = new SentMessageResponse("SentMessageResponse");
					message.setUserid(((SentMessageResponse)msg).getUserid());
					message.setChatroomname(((SentMessageResponse)msg).getChatroomname());
					message.setChatmessage(((SentMessageResponse)msg).getChatmessage());
					message.setTimestamp(((SentMessageResponse)msg).getTimestamp());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "UserLeftChatRoomResponse": {
					UserLeftChatRoomResponse message = new UserLeftChatRoomResponse("UserLeftChatRoomResponse");
					message.setUserid(((UserLeftChatRoomResponse)msg).getUserid());
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
				default: {
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
			if (c.getUuid() != retMsg.getRecipientSessionId()){
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
			boolean added = addUserToChatroom(action.getChatroomname(), action.getUserid());

			((ChatJoinResponse)retMsg).setStatus(added);

			if (added) {
				((ChatJoinResponse)retMsg).setResponse("Joined room successfully");

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

					//Announce the users presence to others in the chat room.
					announceToUsersInChatRoom(action, action.getChatroomname());

				} else {
					((ChatJoinResponse)retMsg).setStatus(false);
					((ChatJoinResponse)retMsg).setResponse("Creating room failed. Try again");
				}

			} catch (SQLException e) {
				e.printStackTrace();
				((ChatJoinResponse)retMsg).setStatus(false);
				((ChatJoinResponse)retMsg).setResponse("Internal server error");
			}

		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

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
				((UserLeftChatRoomResponse)retMsg).setUserid(action.getUserid());

				announceRemovalToUsersInChatRoom(action, action.getChatroomname());
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

				((SentMessageResponse)retMsg).setUserid(action.getUserid());
				((SentMessageResponse)retMsg).setChatroomname(action.getChatroomname());
				((SentMessageResponse)retMsg).setChatmessage(action.getChatmessage());
				((SentMessageResponse)retMsg).setTimestamp(String.valueOf(Instant.now().getEpochSecond()));

				sendMessageToChatRoom(retMsg);
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

	private void UserDoesDiceThrow(Message action){
	        /*int i = 0; //Loop variable

            while (i <= activeLudoGames.size() && i != action.getLudoId()) {
                i++;
            }
	        Ludo selectedGame = activeLudoGames.get(i);
			 //ludo logic
            selectedGame.throwDice(); //Event will be called.
			*/
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

	/**
	 * Send chat message to a chat room
	 * @param action Message object
	 */
	private void sendMessageToChatRoom(Message action){

		for (ChatRoom room : activeChatRooms) { //Loop over chat rooms
			if (room.getName().contentEquals( ((SentMessageResponse) action).getChatroomname()) ){ // Find correct chat room
				for(String UserId : room.getConnectedUsers()){ //Get all active users
					action.setRecipientSessionId(useridToSessionId(UserId)); //Set recipient of message to userid
					synchronized (messagesToSend) {
						messagesToSend.add(action); //Send message.
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
		for (ChatRoom room : activeChatRooms) { //Loop over chat rooms
			if (room.getName().contentEquals(chatroomname)){ // Find correct chat room
				for(String UserId : room.getConnectedUsers()){ //Get all active users
					if (!UserId.contentEquals(sessionIdToUserId(action.getRecipientSessionId()))) {
						Message chatJoinNewUserResponse = new ChatJoinNewUserResponse("ChatJoinNewUserResponse");
						((ChatJoinNewUserResponse)chatJoinNewUserResponse).setUserid(UserId);
						chatJoinNewUserResponse.setRecipientSessionId(useridToSessionId(((ChatJoinNewUserResponse) chatJoinNewUserResponse).getUserid()));

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
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private void announceRemovalToUsersInChatRoom(Message action, String chatroomname){
		for (ChatRoom room : activeChatRooms) { //Loop over chat rooms
			if (room.getName().contentEquals(chatroomname)){ // Find correct chat room
				for(String UserId : room.getConnectedUsers()){ //Get all active users
						Message userLeftChatRoomResponse = new UserLeftChatRoomResponse("UserLeftChatRoomResponse");
						((UserLeftChatRoomResponse)userLeftChatRoomResponse).setUserid(UserId);
						((UserLeftChatRoomResponse)userLeftChatRoomResponse).setChatroomname(chatroomname);
						userLeftChatRoomResponse.setRecipientSessionId(useridToSessionId(((ChatJoinNewUserResponse) userLeftChatRoomResponse).getUserid()));

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

	/**
	 * Implemented interface DiceListener
	 * @param diceEvent returns data about dice rolled
	 */
	@Override
	public void diceThrown(DiceEvent diceEvent) {
		/*
	    //All player ids that we want to return information to.
	    int playerIds[] = new int[4];

	    for(int i = 0; i < 4; i++) {
            JsonMessage retMsg = new JsonMessage();

            // Do Stuff
            retMsg.setAction(JsonMessage.Actions.ServerThrowDice);
            retMsg.setRecipientPlayerid(playerIds[i]);
            retMsg.setPlayerId(diceEvent.getPlayerID());
            retMsg.setLudoId(0); //TODO: This has to be found somehow.
            retMsg.setDiceRolled(diceEvent.getDiceRolled());

            synchronized (messagesToSend){
                messagesToSend.add(retMsg);
            }
        }

	    */

	}

	/**
	 * Implemented interface PieceListener
	 * @param pieceEvent returns data about the piece moved
	 */
	@Override
	public void pieceMoved(PieceEvent pieceEvent) {
	}

	/**
	 * Implemented interface PlayerListener
	 * @param event PlayerEvent containing info about what state the player is in.
	 */
	@Override
	public void playerStateChanged(PlayerEvent event) {

	}


}