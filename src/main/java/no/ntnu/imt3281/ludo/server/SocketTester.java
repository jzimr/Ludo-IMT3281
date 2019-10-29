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
    private boolean connected = false;
    private boolean messageSent = false;


    public static void main(String[] args) {
        new SocketTester();
    }

    public SocketTester(){
        connect();
        //initialize();
        send();
        while(connected) { //As long as we are connected. Force send messages until one of them go through.
            send();
            if (messageSent) { //Once one message is sent we disconnect.
                disconnect();
            }
        }
    }


    void initialize() {
        Thread t = new Thread(() -> {
            while (true) {
                if (connected) {
                    try {
                        final String in = br.readLine();
                        System.out.println(in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void disconnect() {
        try {
            bw.close();
            br.close();
            connection.close();
            connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        String serverName = "127.0.0.1";
        int port = DEFAULT_PORT;
        if (serverName.indexOf(":") > 0) {
            try {
                port = Integer.parseInt(serverName.split(":")[1]);
            } catch (NumberFormatException e) {

            }
            serverName = serverName.split(":")[0];
        }
        try {
            connection = new Socket(serverName, port);
            bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            connected = true;

        } catch (ConnectException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void send() {
        try {
            bw.write("Test Message");
            bw.newLine();
            bw.flush();
            messageSent = true;
        } catch (IOException e) {
            e.printStackTrace();
            messageSent = true;
            System.out.println("Error. Could not send message");
        }
    }
}
