package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.logic.messages.ClientLogin;
import no.ntnu.imt3281.ludo.server.Client;
import no.ntnu.imt3281.ludo.server.Server;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Tests the ClientSocket class by creating a dummy server that writes all the info
 * (that a real server actually should process) back into this class so we can check that they are correct.
 */
public class ClientSocketTest {

    private static final int DEFAULT_PORT = 4567;

    private ClientSocket clientSocket;
    protected Client client;
    private BufferedWriter bw;
    private BufferedReader br;

    /**
     * This one registers the connection between client -> server
     */
    private void serverThread(){
        Thread server = new Thread(() -> {
            try {
                ServerSocket server1 = new ServerSocket(DEFAULT_PORT);
                while (true) {
                    Socket s = server1.accept();
                    try {
                        client = new Client(s);
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

    @Before
    public void setupClientAndServer() {
        // create a new server we listen to
        serverThread();

        // create a new client we want to test
        clientSocket = new ClientSocket();

        // connect to server
        clientSocket.establishConnectionToServer("0.0.0.0", DEFAULT_PORT);
    }

    @Test
    public void establishConnectionToServerTest() {
        try{
            System.out.println(client.read().toString());
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Test
    public void closeConnectionToServer() {
        // todo
    }

    @Test
    public void sendMessageToServer() {
        // todo
        clientSocket.sendMessageToServer(new ClientLogin("UserDoesLoginManual", "Boby", "Boby's Dog"));
        try{
            System.out.println(client.read().toString());
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Test
    public void listenToServer() {
    }

    @Test
    public void handleMessagesFromServer() {
    }
}