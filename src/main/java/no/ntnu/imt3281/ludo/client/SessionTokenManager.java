package no.ntnu.imt3281.ludo.client;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class SessionTokenManager {

    public static class SessionData{
        public String sessionToken;
        public String serverAddress;
    }

    /**
     * Generate a new session token
     *
     * @return a new UUID generated token
     */
    public static String generateSessionToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Write the session to the session file for later reading
     *
     * @param serverAddress the server address that will be written to file
     * @param sessionToken the token that will be written to file
     */
    public static void writeSessionToFile(String serverAddress, String sessionToken) {
        File fout = new File("session.dta");

        try{
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            bw.write(serverAddress);
            bw.newLine();
            bw.write(sessionToken);
            bw.close();
        } catch (IOException e) {
            System.out.println("Could not write to file: " + e.getMessage());
            return;
        }
    }

    /**
     * Read the session from file (if exists)
     *
     * @return object if success, null if not exist or error
     */
    public static SessionData readSessionFromFile() {
        BufferedReader reader;
        SessionData sessionData = new SessionData();

        try{
            reader = new BufferedReader(new FileReader("session.dta"));
            String line = reader.readLine();
            sessionData.serverAddress = line;
            line = reader.readLine();
            sessionData.sessionToken = line;
            reader.close();
            return sessionData;
        } catch(IOException e){
            System.out.println("Could not read from file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete the file where session token is located
     */
    public static void deleteSessionFile() {
        try {
            Files.deleteIfExists(Paths.get("session.dta"));
        } catch (IOException e) {
            System.out.println("Could not delete file: " + e.getMessage());
        }
    }


}
