package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.*;
import no.ntnu.imt3281.ludo.logic.messages.LoginOrRegisterResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
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

	//Might change to arrayList for easy managment.
	LinkedList<Ludo> activeLudoGames = new LinkedList<>();

	LinkedList<Client> clients = new LinkedList<>();
	boolean stopping = false;

	ArrayBlockingQueue<JsonMessage> objectsToHandle = new ArrayBlockingQueue<>(100);

	ArrayBlockingQueue<JsonMessage> messagesToSend = new ArrayBlockingQueue<JsonMessage>(100);

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
									objectsToHandle.add(c.parseUsername(msg)); //Add the object to queue for handling
								}
							} else if (msg != null ) {

								JsonMessageParser parse = new JsonMessageParser(); //Initiate a parser
								JsonMessage json = parse.parseActionJson(msg); //Parse the json into a object
								synchronized (objectsToHandle) {
									objectsToHandle.add(json); //Add the object to queue for handling
									System.out.println(objectsToHandle.size());
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
					JsonMessage msg = messagesToSend.take();
						Iterator<Client> iterator = clients.iterator();
						while (iterator.hasNext()) {
							Client c = iterator.next();
							if (c.getUserId() == msg.getPlayerId() || c.getUsername() == msg.getUsername()) {
								try {
									String converted = convertToCorrectJson(msg);
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
					JsonMessage message = objectsToHandle.take();
					System.out.println(message.getAction());
					handleAction(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		handleActions.start();
	}

	private String convertToCorrectJson(JsonMessage msg) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String msgJson = mapper.writeValueAsString(msg);

			switch (msg.getAction()) {
				case "LoginStatus" : case "RegisterStatus":{
					LoginOrRegisterResponse ret ;
					ret = mapper.readValue(msgJson, LoginOrRegisterResponse.class);
					ret.setLoginStatus(msg.getLoginOrRegisterStatus());
					String retString = mapper.writeValueAsString(ret);
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


	private void handleAction(JsonMessage action){
		switch (action.getAction()) {
			case "UserDoesDiceThrow": UserDoesDiceThrow(action); break;
			case "UserDoesLoginManual": UserDoesLoginManual(action); break;
			case "UserDoesLoginAuto": UserDoesLoginAuto(action); break;
			case "UserDoesRegister": UserDoesRegister(action);break;
		}

	}


	private void UserDoesLoginManual(JsonMessage action){

		JsonMessage retMsg = new JsonMessage();
		retMsg.setAction(JsonMessage.Actions.LoginStatus);
		try {
			boolean status = db.checkIfLoginValid(action.getUsername(), action.getPassword());
			retMsg.setLoginOrRegisterStatus(status);

		} catch (SQLException e) {
			retMsg.setLoginOrRegisterStatus(false);
			e.printStackTrace();
		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	private void UserDoesLoginAuto(JsonMessage action){

		JsonMessage retMsg = new JsonMessage();
		retMsg.setAction(JsonMessage.Actions.LoginStatus);
		try {
			boolean status = db.checkIfLoginValid(String.valueOf(action.getPlayerId()),action.getUsername(), action.getPassword());
			retMsg.setLoginOrRegisterStatus(status);

		} catch (SQLException e) {
			retMsg.setLoginOrRegisterStatus(false);
			e.printStackTrace();
		}

		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	private void UserDoesRegister(JsonMessage action){

		JsonMessage retMsg = new JsonMessage();
		retMsg.setAction(JsonMessage.Actions.RegisterStatus);
		System.out.println(action.getUsername());
		System.out.println(action.getPassword());
		try {
			db.insertAccount(action.getUsername(), action.getPassword());
			retMsg.setLoginOrRegisterStatus(true);
		} catch (SQLException e) {
			retMsg.setLoginOrRegisterStatus(false);
			e.printStackTrace();
		}
		synchronized (messagesToSend) {
			messagesToSend.add(retMsg);
		}

	}

	private void UserDoesDiceThrow(JsonMessage action){
			/* ludo logic */



			/* Do Stuff */
			JsonMessage retMsg = new JsonMessage();
			retMsg.setAction(JsonMessage.Actions.ServerThrowDice);
			retMsg.setPlayerId(action.getPlayerId());
			retMsg.setLudoId(action.getLudoId());
			retMsg.setDiceRolled(2);
			synchronized (messagesToSend){
				messagesToSend.add(retMsg);
			}
			/* Send message back to original sender */
	}

	public class JsonMessageParser {
		ObjectMapper mapper = new ObjectMapper();

		public JsonMessage parseActionJson(String json) {

			try {
				JsonMessage jsonObj = mapper.readValue(json, JsonMessage.class);
				return jsonObj;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	public class activeLudoGame{
		Ludo game;
		int[] players;

		void setGame (Ludo game) {
			this.game = game;
		}

		Ludo getGame() {
			return this.game;
		}

	}


	@Override
	public void diceThrown(DiceEvent diceEvent) {
		/* Logic for throw dice in ludo class */
		/* Ludo game = diceEvent.getLudoGame();
		game.addDiceListener(this);*/

		/* Add messages that are to be sent to users in the game. */

	}

	@Override
	public void pieceMoved(PieceEvent pieceEvent) {

	}

	@Override
	public void playerStateChanged(PlayerEvent event) {

	}


}


