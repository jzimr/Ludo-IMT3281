package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.logic.JsonMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
public class Server {

	final private int SERVER_PORT = 4567;

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
							System.out.println(clients.size());
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
							if (msg != null) {

								JsonMessageParser parse = new JsonMessageParser(); //Initiate a parser
								JsonMessage json = parse.parseActionJson(msg); //Parse the json into a object
								synchronized (objectsToHandle) {
									objectsToHandle.add(json); //Add the object to queue for handling
								}
								c.send("{\"ack\":true}"); //Acknowledgment that the server got the packet.

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
					synchronized (clients) {
						Iterator<Client> iterator = clients.iterator();
						while (iterator.hasNext()) {
							Client c = iterator.next();

								try {
									c.send("HeiHei");
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

					/*
					Handle message logic here.
					Have to be moved to a seperate function. Only for testing purposes for now.
					 */

					switch(message.getAction()) {
						case "UserDoesDiceThrow": break;
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		handleActions.start();
	}

	/**
	 * Represents a client. Contains the open socket and input and output from that user.
	 */
	class Client {
		int userId;

		Socket s;
		BufferedWriter bw;
		BufferedReader br;

		public Client (Socket s) throws IOException {
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		}

		public String read() throws IOException {
			if (br.ready()) {
				return br.readLine();
			}
			return null;
		}

		public void send(String s) throws IOException {
			bw.write(s);
			bw.newLine();
			bw.flush();
		}

		public void close() throws IOException {
			bw.close();
			br.close();
			s.close();
		}
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


}


