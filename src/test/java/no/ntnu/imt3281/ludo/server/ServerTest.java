package no.ntnu.imt3281.ludo.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;

public class ServerTest {


    private static final int DEFAULT_PORT = 4567;

    private Server server;
    private Socket connection = null;
    private BufferedWriter bw;
    private BufferedReader br;

    @Before
    public void setupServerAndClient() {
        server = new Server();

        //establish socket connection to server
        try {
            connection = new Socket("127.0.0.1", DEFAULT_PORT);
            bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void shutDownServerAndClient(){
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.stopServer();

    }

    @Test
    public void clientGetsResponse(){
        String IdentificationMessage = "{\"userId\": 1}";
        String message = "{\"eventTest\":\"test\"}";

        try {
            bw.write(IdentificationMessage);
            bw.newLine();
            bw.flush();

            String gotMessage = br.readLine();
            System.out.println("Acknowledge message: " + gotMessage); //Mainly for debugging purposes

            gotMessage = br.readLine();
            System.out.println(" " + gotMessage); //Wait for server to process message.
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
