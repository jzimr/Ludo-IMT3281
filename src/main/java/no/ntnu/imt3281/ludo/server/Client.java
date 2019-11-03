package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.JsonMessage;

import java.io.*;
import java.net.Socket;

class Client {
    ObjectMapper mapper = new ObjectMapper();
    int userId;
    String username;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId(){
        return this.userId;
    }

    public void parseUsername(String json) {

        try {
            JsonNode jsonNode = mapper.readTree(json);
            username = jsonNode.get("username").asText();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
