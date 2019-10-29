package no.ntnu.imt3281.ludo.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
public class Server {

	final private int SERVER_PORT = 4567;

	LinkedList<Client> clients = new LinkedList<>();
	boolean stopping = false;
	ArrayBlockingQueue<Message> messagesToSend = new ArrayBlockingQueue<Message>(100);
	ArrayBlockingQueue<Client> disconnectedClients = new ArrayBlockingQueue<>(1000);

	public static void main(String[] args) {
		new Server();
	}

	public Server(){
		startServerThread();
		startListener();
		//startSenderThread();
		//startRemoveDisconnectedClientsThread();
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
								//messagesToSend.add(new Message(msg, c.name));
								System.out.println("Message from some client:" + msg);
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
					synchronized (clients) {
						Iterator<Client> iterator = clients.iterator();
						while (iterator.hasNext()) {
							Client c = iterator.next();

								try {
									c.send(msg.message);
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
					Message msg = new Message("Vanished into thin air");
					synchronized (clients) {
						clients.remove(client);
					}
					messagesToSend.add(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		removeDisconnectedClientsThread.setDaemon(true);
		removeDisconnectedClientsThread.start();
	}


	/**
	 * Represents a client. Contains the open socket and input and output from that user.
	 */
	class Client {
		Socket s;
		BufferedWriter bw;
		BufferedReader br;
		String name;

		public Client (Socket s) throws IOException {
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			name = br.readLine();
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

	/**
	 * Class copied from the example on bitbucket. To be removed.
	 */
	private class Message {
		String message;
		public Message(String msg) {
			message = msg;
		}
	}

}
