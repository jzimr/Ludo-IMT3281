package no.ntnu.imt3281.ludo.client;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SessionTokenManager {
    public static void writeSessionToken(String filename, String sessionToken) throws IOException {
        byte[] sessionData = sessionToken.getBytes();
        FileOutputStream out = new FileOutputStream(filename);
        out.write(sessionData);
        out.close();
    }

    public static String readSessionToken(String filename) throws IOException{
        byte[] data = Files.readAllBytes(Paths.get(filename));

        return new String(data);
    }


}
