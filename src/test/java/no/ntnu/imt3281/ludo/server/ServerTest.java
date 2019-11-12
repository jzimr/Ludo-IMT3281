package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServerTest {


    private static final int DEFAULT_PORT = 4567;

    private static Server server;

    private static Socket connection_client_1 = null;
    private static BufferedWriter bw_client_1;
    private static BufferedReader br_client_1;


    private static Socket connection_client_2 = null;
    private static BufferedWriter bw_client_2;
    private static BufferedReader br_client_2;

    private static String client_1_session = "458b2331-14f4-419f-99b1-ad492e8906fb";
    private static String client_2_session = "348b2331-14f4-419f-99b1-ad492e8906fa";

    private static String client_1_userid;
    private static String client_2_userid;

    private static String gameid;

    @BeforeClass
    public static void setupServerAndClient() throws IOException {
        System.out.println("Wiping DB... Staring server... Starting connections...");
        //Wipe db and setup server.

        try {
            Connection testConnection = DriverManager.getConnection("jdbc:derby:./ludoTestDB");
            Statement statement = testConnection.createStatement();
            statement.execute("DROP TABLE chat_log");
            statement.execute("DROP TABLE chat_room");
            statement.execute("DROP TABLE session_info");
            statement.execute("DROP TABLE user_info");
            statement.execute("DROP TABLE login_info");
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertFalse(true);
        }

        server = new Server(true);

        //establish socket connection to server
        try {
            connection_client_1 = new Socket("127.0.0.1", DEFAULT_PORT);
            bw_client_1 = new BufferedWriter(new OutputStreamWriter(connection_client_1.getOutputStream()));
            br_client_1 = new BufferedReader(new InputStreamReader(connection_client_1.getInputStream()));

            connection_client_2 = new Socket("127.0.0.1", DEFAULT_PORT);
            bw_client_2 = new BufferedWriter(new OutputStreamWriter(connection_client_2.getOutputStream()));
            br_client_2 = new BufferedReader(new InputStreamReader(connection_client_2.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String RegisterMessage_client_1 = "{\"action\" : \"UserDoesRegister\",\"username\": \"test\", \"recipientSessionId\":\""+ client_1_session + "\" ,\"password\": \"test\"}";
        String RegisterMessage_client_2 = "{\"action\" : \"UserDoesRegister\",\"username\": \"test2\", \"recipientSessionId\":\""+ client_2_session + "\" ,\"password\": \"test2\"}";

        bw_client_1.write(RegisterMessage_client_1);
        bw_client_1.newLine();
        bw_client_1.flush();

        String gotMessage = br_client_1.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("Registration successful"));

        bw_client_2.write(RegisterMessage_client_2);
        bw_client_2.newLine();
        bw_client_2.flush();

        gotMessage = br_client_2.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("Registration successful"));

        String LoginMessage_client_1 = "{\"action\" : \"UserDoesLoginManual\" ,\"username\": \"test\" ,\"recipientSessionId\":\""+client_1_session+"\" ,\"password\": \"test\"}";
        String LoginMessage_client_2 = "{\"action\" : \"UserDoesLoginManual\" ,\"username\": \"test2\" ,\"recipientSessionId\":\""+client_2_session+"\" ,\"password\": \"test2\"}";

        bw_client_1.write(LoginMessage_client_1);
        bw_client_1.newLine();
        bw_client_1.flush();

        gotMessage = br_client_1.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("\"loginStatus\":true"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userid = mapper.readTree(gotMessage);
        client_1_userid = userid.get("userid").asText();

        bw_client_2.write(LoginMessage_client_2);
        bw_client_2.newLine();
        bw_client_2.flush();

        gotMessage = br_client_2.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("\"loginStatus\":true"));
        userid = mapper.readTree(gotMessage);
        client_2_userid = userid.get("userid").asText();
    }

    @AfterClass
    public static void shutDownServerAndClient(){

        try {
            connection_client_1.close();
            connection_client_2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.stopServer();
    }

    @Test
    public void AclientJoinChatRoom() throws IOException{

        String JoinChatMessage_client_1 = "{\"action\" : \"UserJoinChat\", \"userid\" : \"" + client_1_userid + "\", \"chatroomname\" : \"Global\"}";
        String JoinChatMessage_client_1_new = "{\"action\" : \"UserJoinChat\", \"userid\" : \"" + client_1_userid + "\", \"chatroomname\" : \"Global New\"}";
        String JoinChatMessage_client_2 = "{\"action\" : \"UserJoinChat\", \"userid\" : \"" + client_2_userid + "\", \"chatroomname\" : \"Global\"}";

        bw_client_1.write(JoinChatMessage_client_1);
        bw_client_1.newLine();
        bw_client_1.flush();

        String gotMessage = br_client_1.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contentEquals("{\"action\":\"ChatJoinResponse\",\"status\":true,\"response\":\"Joined room successfully\",\"chatroomname\":\"Global\"}"));

        bw_client_2.write(JoinChatMessage_client_2);
        bw_client_2.newLine();
        bw_client_2.flush();

        gotMessage = br_client_2.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contentEquals("{\"action\":\"ChatJoinResponse\",\"status\":true,\"response\":\"Joined room successfully\",\"chatroomname\":\"Global\"}"));

        gotMessage = br_client_1.readLine();
        assertTrue(gotMessage.contentEquals("{\"action\":\"ChatJoinNewUserResponse\",\"displayname\":\"test2\",\"chatroomname\":\"Global\"}"));

        //Make user join a new chatroom which does not exist.
        bw_client_1.write(JoinChatMessage_client_1_new);
        bw_client_1.newLine();
        bw_client_1.flush();

        gotMessage = br_client_1.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contentEquals("{\"action\":\"ChatJoinResponse\",\"status\":true,\"response\":\"Room created and joined successfully\",\"chatroomname\":\"Global New\"}"));



    }

    @Test
    public void BclientSendChatMessage() throws IOException{

        String ChatMessage_client_1 = "{ \"action\": \"UserSentMessage\", \"userid\": \""+client_1_userid+"\" ,\"chatroomname\": \"Global\" , \"chatmessage\" : \"Hei\" }";
        String ChatMessage_client_2 = "{ \"action\": \"UserSentMessage\", \"userid\": \""+client_2_userid+"\" ,\"chatroomname\": \"Global\" , \"chatmessage\" : \"Hei ja\" }";

        bw_client_1.write(ChatMessage_client_1);
        bw_client_1.newLine();
        bw_client_1.flush();

        String gotMessage = br_client_1.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("{\"action\":\"SentMessageResponse\",\"displayname\":\"test\",\"chatroomname\":\"Global\",\"chatmessage\":\"Hei\",\"timestamp\":\""));

        bw_client_2.write(ChatMessage_client_2);
        bw_client_2.newLine();
        bw_client_2.flush();

        gotMessage = br_client_2.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("{\"action\":\"SentMessageResponse\",\"displayname\":\"test\",\"chatroomname\":\"Global\",\"chatmessage\":\"Hei\",\"timestamp\":\""));

        gotMessage = br_client_2.readLine();
        System.out.println("Got Message: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("{\"action\":\"SentMessageResponse\",\"displayname\":\"test2\",\"chatroomname\":\"Global\",\"chatmessage\":\"Hei ja\",\"timestamp\":\""));

        gotMessage = br_client_1.readLine();
        assertTrue(gotMessage.contains("{\"action\":\"SentMessageResponse\",\"displayname\":\"test2\",\"chatroomname\":\"Global\",\"chatmessage\":\"Hei ja\",\"timestamp\":\""));

    }

    @Test
    public void CclientsJoinLobby() throws IOException{
        String CreateGame_client_1 = "{\"action\":\"UserWantsToCreateGame\", \"hostid\": \""+client_1_userid+"\", \"toinvitedisplaynames\": [\"test2\"]}";

        bw_client_1.write(CreateGame_client_1);
        bw_client_1.newLine();
        bw_client_1.flush();

        String gotMessage = br_client_1.readLine();
        System.out.println("Got Message Client1 : " + gotMessage); //Mainly for debugging purposes

        ObjectMapper mapper = new ObjectMapper();
        JsonNode gameid_client1 = mapper.readTree(gotMessage);
        gameid = gameid_client1.get("gameid").asText();

        //System.out.println("Gameid " +gameid + " " + AcceptGame_client_2 );
        String AcceptGame_client_2 = "{\"action\":\"UserDoesGameInvitationAnswer\", \"accepted\": true, \"userid\":\""+client_2_userid+"\", \"gameid\":\""+gameid+"\"}";
        gotMessage = br_client_2.readLine();
        System.out.println("Got Message Client 2: " + gotMessage); //Mainly for debugging purposes

        bw_client_2.write(AcceptGame_client_2);
        bw_client_2.newLine();
        bw_client_2.flush();
        System.out.println("sent " + gameid);

        gotMessage = br_client_1.readLine();
        System.out.println("Got Message Client1 : " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("\"playersinlobby\":[\"test\",\"test2\"]}"));

        gotMessage = br_client_2.readLine();
        System.out.println("Got Message Client 2: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("\"playersinlobby\":[\"test\",\"test2\"]}"));

        gotMessage = br_client_2.readLine();
        System.out.println("Got Message Client 2: " + gotMessage); //Mainly for debugging purposes
        gotMessage = br_client_2.readLine();
        System.out.println("Got Message Client 2: " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("{\"action\":\"GameHasStartedResponse\",\"gameid\":"));


    }

    @Test
    public void DclientsDeclinesInvite() throws IOException{
        String CreateGame_client_1 = "{\"action\":\"UserWantsToCreateGame\", \"hostid\": \""+client_1_userid+"\", \"toinvitedisplaynames\": [\"test2\"]}";

        bw_client_1.write(CreateGame_client_1);
        bw_client_1.newLine();
        bw_client_1.flush();

        String gotMessage = br_client_1.readLine();
        System.out.println("Got Message Client1 : " + gotMessage); //Mainly for debugging purposes

        ObjectMapper mapper = new ObjectMapper();
        JsonNode gameid_client1 = mapper.readTree(gotMessage);
        gameid = gameid_client1.get("gameid").asText();

        //System.out.println("Gameid " +gameid + " " + AcceptGame_client_2 );
        String AcceptGame_client_2 = "{\"action\":\"UserDoesGameInvitationAnswer\", \"accepted\": false, \"userid\":\""+client_2_userid+"\", \"gameid\":\""+gameid+"\"}";
        gotMessage = br_client_2.readLine();
        System.out.println("Got Message Client 2: " + gotMessage); //Mainly for debugging purposes

        bw_client_2.write(AcceptGame_client_2);
        bw_client_2.newLine();
        bw_client_2.flush();
        System.out.println("sent " + gameid);

        gotMessage = br_client_1.readLine();
        System.out.println("Got Message Client1 : " + gotMessage); //Mainly for debugging purposes
        assertTrue(gotMessage.contains("{\"action\":\"UserDeclinedGameInvitationResponse\",\"accepted\":false"));

    }

    //Lobby system.


}
