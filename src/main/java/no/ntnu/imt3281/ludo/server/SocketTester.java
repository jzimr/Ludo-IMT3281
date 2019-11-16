package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.*;
import java.io.*;
import java.util.UUID;

/**
 * This code is only used to test message handling from the client without having
 * a client fully implemented.
 */

public class SocketTester {

    private static final int DEFAULT_PORT = 4567;

    UUID uuid = UUID.randomUUID();

    private Socket connection = null;
    private BufferedWriter bw;
    private BufferedReader br;

    private String gameid;

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

                //sendRegister();
                sendLogin();
                //sendAutoLogin();

                //joinChatRoom();

                //listChatRooms();

                //listUserList();

                //createGameRequest();
                //acceptGameInvite();

                //sendChatMessage();

                //removeFromChatRoom();

                sendUserView();
                sendUserEdit();
                sendUserView();

                //sendChatMessage();
                //sendChatMessage();
                while(true){
                    String gotMessage = br.readLine();
                    if (!gotMessage.contains("Ping")) {
                        System.out.println(gotMessage);
                    }
                }

                /*gotMessage = br.readLine();
                System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes*/

                //Thread.sleep(100);
                //connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendRegister(){
            String RegisterMessage = "{\"action\" : \"UserDoesRegister\",\"username\": \"test5\", \"recipientSessionId\":\"458b2331-14f4-419f-99b1-ad492e8906fc\" ,\"password\": \"test5\"}";

            try {
                bw.write(RegisterMessage);
                bw.newLine();
                bw.flush();
                System.out.println("Sent message : " + RegisterMessage);

                String gotMessage = br.readLine();
                System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    private void sendLogin(){
        String LoginMessage = "{\"action\" : \"UserDoesLoginManual\" ,\"username\": \"test\" ,\"recipientSessionId\":\"458b2331-14f4-419f-99b1-ad492e8906fb\" ,\"password\": \"test\"}";
        //String LoginMessage = "{\"recipientuuid\":null ,\"action\" : \"U1serDoesLoginManual\" ,\"username\": \"test\" ,\"recipientSessionId\":\"458b2331-14f4-419f-99b1-ad492e8906fb\" ,\"password\": \"test\"}";

        try {
            bw.write(LoginMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + LoginMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAutoLogin(){
         String LoginMessage = "{\"action\" : \"UserDoesLoginAuto\" , \"recipientSessionId\":\"458b2331-14f4-419f-99b1-ad492e8906fb\"}";

        try {
            bw.write(LoginMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + LoginMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void joinChatRoom(){
        String JoinChatMessage = "{\"action\" : \"UserJoinChat\", \"userid\" : \"2ecc4deb-e320-4fac-9834-2ee0a84edeca\", \"chatroomname\" : \"Global\"}";
        try {
            bw.write(JoinChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + JoinChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes

            Thread.sleep(500);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendChatMessage(){
        String ChatMessage = "{ \"action\": \"UserSentMessage\", \"userid\": \"2ecc4deb-e320-4fac-9834-2ee0a84edeca\" ,\"chatroomname\": \"Global\" , \"chatmessage\" : \"heisann\" }";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes

            Thread.sleep(500);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void removeFromChatRoom(){
        String ChatMessage = "{\"action\":\"UserLeftChatRoom\",\"userid\":\"2ecc4deb-e320-4fac-9834-2ee0a84edeca\" ,\"chatroomname\":\"Global\"}";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listChatRooms(){
        String ChatMessage = "{\"action\":\"UserListChatrooms\"}";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void listUserList(){
        String ChatMessage = "{\"action\":\"UserWantsUsersList\", \"userid\": \"2ecc4deb-e320-4fac-9834-2ee0a84edeca\", \"searchquery\":\"tes\"}";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createGameRequest(){
        String ChatMessage = "{\"action\":\"UserWantsToCreateGame\", \"hostid\": \"2ecc4deb-e320-4fac-9834-2ee0a84edeca\", \"toinvitedisplaynames\": [\"test\"]}";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes

            ObjectMapper mapper = new ObjectMapper();
            JsonNode gameid_json = mapper.readTree(gotMessage);
            gameid = gameid_json.get("gameid").asText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptGameInvite(){
        String ChatMessage = "{\"action\":\"UserDoesGameInvitationAnswer\", \"accepted\": true, \"userid\":\"2ecc4deb-e320-4fac-9834-2ee0a84edeca\", \"gameid\":\""+gameid+"\"}";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUserEdit(){
        String ChatMessage = "{\"action\":\"UserWantToEditProfile\",\"displayname\":\"test\",\"imagestring\":\"data:image/jpeg;base64,/9j/4QAYRXhpZgAASUkqAAgAAAAAAAAAAAAAAP/sABFEdWNreQABAAQAAABkAAD/4QMraHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/PiA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzYgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOkMwMTIwN0NEMDg3NjExRTRBRTlDOTkzQjM4RTdBMDY4IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOkMwMTIwN0NFMDg3NjExRTRBRTlDOTkzQjM4RTdBMDY4Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6QzAxMjA3Q0IwODc2MTFFNEFFOUM5OTNCMzhFN0EwNjgiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6QzAxMjA3Q0MwODc2MTFFNEFFOUM5OTNCMzhFN0EwNjgiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7/7gAOQWRvYmUAZMAAAAAB/9sAhAABAQEBAQEBAQEBAQEBAQEBAQEB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\",\"password\":\"\"}";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUserView(){
        String ChatMessage = "{\"action\":\"UserWantToViewProfile\",\"displayname\":\"test\"}";

        try {
            bw.write(ChatMessage);
            bw.newLine();
            bw.flush();
            System.out.println("Sent message : " + ChatMessage);

            String gotMessage = br.readLine();
            System.out.println("Recieved: " + gotMessage); //Mainly for debugging purposes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}