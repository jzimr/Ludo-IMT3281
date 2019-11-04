package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.annotation.JsonInclude;
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
 * 
 * This is the main class for the server. 
 * **Note, change this to extend other classes if desired.**
 * 
 * @author 
 *
 */
public class Server implements DiceListener, PieceListener, PlayerListener {

	final private int SERVER_PORT = 4567;
	Database db = Database.getDatabase();

	private static SHA512Hasher hasher = new SHA512Hasher();    // our hasher object for hashing passwords

	ArrayList<Ludo> activeLudoGames = new ArrayList<>();

	LinkedList<Client> clients = new LinkedList<>();
	boolean stopping = false;

	ArrayBlockingQueue<Message> objectsToHandle = new ArrayBlockingQueue<>(100);

	ArrayBlockingQueue<Message> messagesToSend = new ArrayBlockingQueue<>(100);

	ArrayBlockingQueue<Client> disconnectedClients = new ArrayBlockingQueue<>(1000);

	public static void main(String[] args) {
		new Server();
	}

	public Server(){
		startServerThread();
		startListener();
		startHandlingActions();
		startSenderThread();
		startRemoveDisconnectedClientsThread();

		System.out.println("Ludo server is now listening at 0.0.0.0:"+SERVER_PORT);

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

							if (msg != null && msg.contains("UserDoesLogin")) {
								synchronized (objectsToHandle) {
									c.parseUsername(msg);
									System.out.println("Connected user : " + c.getUsername());
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
							System.out.println("User: " + msg.getRecipientUsername());
							if (msg.getRecipientUsername().contentEquals(c.getUsername())) {
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
 	*
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

	private void handleAction(Message action){
		switch (action.getAction()) {
			case "UserDoesDiceThrow": UserDoesDiceThrow(action);break;
			case "UserDoesLoginManual": UserDoesLoginManual((ClientLogin) action);break;
			case "UserDoesLoginAuto": UserDoesLoginAuto((ClientLogin) action); break;
			case "UserDoesRegister": UserDoesRegister((ClientLogin) action); break;
		}

	}

	private String convertToCorrectJson(Message msg) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String msgJson = mapper.writeValueAsString(msg);

			JsonNode jsonNode = mapper.readTree(msgJson);
			String action = jsonNode.get("action").asText();

			switch (action) {
				case "LoginStatus" : case "RegisterStatus":{
					LoginOrRegisterResponse message = new LoginOrRegisterResponse("LoginOrRegisterStatus");
					message.setLoginOrRegisterStatus(( (LoginOrRegisterResponse) msg) .isLoginOrRegisterStatus());
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

	private void UserDoesLoginManual(ClientLogin action){

		LoginOrRegisterResponse retMsg = new LoginOrRegisterResponse("LoginStatus");
		retMsg.setRecipientUsername(action.getUsername());
		try {
			boolean status = db.checkIfLoginValid(action.getUsername(), action.getPassword());
			retMsg.setLoginOrRegisterStatus(status);

		} catch (SQLException e) {
			retMsg.setLoginOrRegisterStatus(false);
			e.printStackTrace();
		}

		System.out.println("UserDoesLoginManual : " + retMsg.getAction());

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

		if (retMsg.isLoginOrRegisterStatus()){
			AnnounceUserLoggedOn(action);
		}

	}

	private void UserDoesLoginAuto(ClientLogin action){

		LoginOrRegisterResponse retMsg = new LoginOrRegisterResponse("LoginStatus");
		retMsg.setRecipientUsername(action.getUsername());

					/*
		try {
			//TODO: This does currently not work. Avoid the usage.
			boolean status = db.checkIfLoginValid(String.valueOf("0"),action.getUsername(), action.getPassword());
			retMsg.setLoginOrRegisterStatus(status);

		} catch (SQLException e) {
			retMsg.setLoginOrRegisterStatus(false);
			e.printStackTrace();
		}
		*/

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

		if (retMsg.isLoginOrRegisterStatus()){
			AnnounceUserLoggedOn(action);
		}
	}

	private void AnnounceUserLoggedOn(Message action){
		Iterator<Client> iterator = clients.iterator();
		while(iterator.hasNext()){
			Client c = iterator.next();

			Message retMsg = new UserHasConnected("UserHasConnected");
			retMsg.setRecipientUsername(c.getUsername());
			retMsg.setRecipientId(c.getUserId());
			((UserHasConnected) retMsg).setUsername(( (ClientLogin) action) .getUsername() );
			//retMsg.setPlayerId(action.getPlayerId());

			//No need to announce to the originator of the message.
			if (retMsg.getRecipientUsername() != ((UserHasConnected)retMsg).getUsername() /*|| retMsg.getRecipientId() != ((ClientLogin)retMsg).getPlayerId())*/){
				synchronized (messagesToSend) {
					messagesToSend.add(retMsg);
				}
			}
		}
	}

	private void UserDoesRegister(ClientLogin action){

		Message retMsg = new LoginOrRegisterResponse("RegisterStatus");
		retMsg.setRecipientUsername(action.getUsername());
		try {
			db.insertAccount(action.getUsername(), action.getPassword());
			((LoginOrRegisterResponse)retMsg).setLoginOrRegisterStatus(true);
		} catch (SQLException e) {
			((LoginOrRegisterResponse)retMsg).setLoginOrRegisterStatus(false);
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

	public class JsonMessageParser {
		ObjectMapper mapper = new ObjectMapper();

		public Message parseJson(String json) {
			Message msg = null;
			try {
				JsonNode action = mapper.readTree(json);

				switch(action.get("action").asText()) {
					case "UserDoesLoginManual":{
						msg = new ClientLogin(action.get("action").asText(),action.get("username").asText(),action.get("password").asText());
						return msg;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

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

	@Override
	public void pieceMoved(PieceEvent pieceEvent) {
	}

	@Override
	public void playerStateChanged(PlayerEvent event) {

	}


}


