package no.ntnu.imt3281.ludo.server;

import java.net.*;
import java.io.*;

/**
 * This code is only used to test message handling from the client without having
 * a client fully implemented.
 */

public class SocketTester {

    private static final int DEFAULT_PORT = 4567;

    private Socket connection = null;
    private BufferedWriter bw;
    private BufferedReader br;

    private String LoginMessage = "{\"action\" : \"UserDoesLoginManual\",\"username\": \"test\",\"password\": \"test\"}";
    String RegisterMessage = "{\"action\" : \"UserDoesRegister\",\"username\": \"test\",\"password\": \"test\"}";

    private String message = "{\"action\" : \"UserDoesDiceThrow\", \"playerId\": 1, \"ludoId\" : 2}";


    public static void main(String[] args) {
        new SocketTester();
    }

    public SocketTester(){
            //establish socket connection to server
            try {
                connection = new Socket("127.0.0.1", DEFAULT_PORT);
                bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                bw.write(LoginMessage);
                bw.newLine();
                bw.flush();
                System.out.println("Sent message : " + LoginMessage);

                String gotMessage = br.readLine();
                System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes

                gotMessage = br.readLine();
                System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes

                Thread.sleep(100);
                connection.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

}
