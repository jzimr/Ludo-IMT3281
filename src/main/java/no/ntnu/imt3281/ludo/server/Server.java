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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This is the main class for the server.
 */
public class Server implements DiceListener, PieceListener, PlayerListener {

	final private int SERVER_PORT = 4567; //Server Port
	Database db = Database.getDatabase(); //Database singleton

	ArrayList<Ludo> activeLudoGames = new ArrayList<>(); //ArrayList over active games.
	ArrayList<String> activeChatRooms = new ArrayList<>(); //ArrayList over chat rooms.

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
		startRemoveDisconnectedClientsThread();

		System.out.println("Ludo server is now listening at 0.0.0.0:"+SERVER_PORT);

		activeChatRooms = db.getAllChatRooms();
		System.out.println(activeChatRooms);

	}

	public void stopServer(){
		stopping = true;
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
									objectsToHandle.add(parser.parseJson(msg)); //Add the object to queue for handling
								}
							} else if (msg != null ) {
								synchronized (objectsToHandle) {
									objectsToHandle.add(parser.parseJson(msg)); //Add the object to queue for handling
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
						//TODO: Send back to user with ID or SessionID:
						System.out.println("Recipientid: " + msg.getRecipientSessionId());
						System.out.println("Clientid : " + c.getUuid());
						if (msg.getRecipientSessionId().contentEquals(c.getUuid())) {
							System.out.println("Sender : " + msg.getAction());
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
					message.setReponse(((LoginResponse) msg).getReponse());
					message.setUserid(((LoginResponse) msg).getUserid());
					String retString = mapper.writeValueAsString(message);
					return retString;
				}
				case "RegisterResponse": {
					RegisterResponse message = new RegisterResponse("RegisterResponse");
					message.setRegisterStatus(( (RegisterResponse) msg) .isRegisterStatus());
					message.setReponse(((RegisterResponse) msg).getReponse());
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
				case "UserHasConnected" : {
					UserHasConnected message = new UserHasConnected("UserHasConnected");
					message.setUsername(((UserHasConnected)msg).getUsername());
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
				retMsg.setReponse("OK");

				String userid = db.getUserId(action.getUsername());
				retMsg.setUserid(userid);
				setUseridToClient(action.getRecipientSessionId(), userid);
				System.out.println("userid : " + retMsg.getUserid());

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
				retMsg.setReponse("Username and/or password are incorrect");
			}

		} catch (SQLException e) {
			retMsg.setReponse("Internal Server Error");
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
				retMsg.setReponse("Login was successful");
				retMsg.setUserid(db.getUserIdBySession(retMsg.getRecipientSessionId()));
			} else {
				retMsg.setReponse("Session token are invalid. Try again");
			}

		} catch (SQLException e) {
			retMsg.setLoginStatus(false);
			retMsg.setReponse("Internal server error");
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
	// TODO: This is not currently correct.
	private void AnnounceUserLoggedOn(Message action){
		Iterator<Client> iterator = clients.iterator();
		while(iterator.hasNext()){
			Client c = iterator.next();

			Message retMsg = new UserHasConnected("UserHasConnected");
			retMsg.setRecipientSessionId(c.getUuid());
			((UserHasConnected) retMsg).setUsername(( (ClientLogin) action) .getUsername() );
			//TODO: SET username and playerid later.
			//retMsg.setPlayerId(action.getPlayerId());

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
				((RegisterResponse)retMsg).setReponse("Registration successful");
			} else {
				((RegisterResponse)retMsg).setRegisterStatus(false);
				((RegisterResponse)retMsg).setReponse("User with username " + action.getUsername() + " already exists");
			}

		} catch (SQLException e) {
			((RegisterResponse)retMsg).setRegisterStatus(false);
			((RegisterResponse)retMsg).setReponse("Internal server error");
			e.printStackTrace();
		}
		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
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
			if (client.getUuid() == sessionId) {
				return client.getUserId();
			}
		}

		return null;
	}

	private void setUseridToClient(String sessionId , String userid){
		Iterator<Client> c = clients.iterator();
		while(c.hasNext()) {
			Client client = c.next();
			if (client.getUuid() == sessionId) {
				client.setUserId(userid);
				return;
			}
		}
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