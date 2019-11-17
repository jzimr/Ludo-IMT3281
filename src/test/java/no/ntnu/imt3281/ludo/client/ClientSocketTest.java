package no.ntnu.imt3281.ludo.client;

import no.ntnu.imt3281.ludo.gui.ServerListeners.LoginResponseListener;
import no.ntnu.imt3281.ludo.gui.ServerListeners.RegisterResponseListener;
import no.ntnu.imt3281.ludo.logic.messages.ClientLogin;
import no.ntnu.imt3281.ludo.server.Client;
import no.ntnu.imt3281.ludo.server.Server;
import org.junit.Before;
import org.junit.BeforeClass;
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

    private static ClientSocket connection_client_1 = null;
    private static BufferedWriter bw_client_1;

    private static ServerSocket server;


    @BeforeClass
    public static void setupClientAndServer() {
        // create a new server we listen to
        connection_client_1 = new ClientSocket();
        try{
            server = new ServerSocket(DEFAULT_PORT);
        } catch(IOException e){
            e.printStackTrace();
        }

        connection_client_1 = new ClientSocket();
        connection_client_1.establishConnectionToServer("0.0.0.0", DEFAULT_PORT);







        //connection_client_1.establishConnectionToServer();


        // create a new client we want to test
        //clientSocket = new ClientSocket();

        // connect to server
        //clientSocket.establishConnectionToServer("0.0.0.0", DEFAULT_PORT);
    }

}