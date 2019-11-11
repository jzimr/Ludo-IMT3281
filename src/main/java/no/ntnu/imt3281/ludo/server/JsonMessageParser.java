package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.messages.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Class that parses incoming messages and classifies them.
 */
public class JsonMessageParser {
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses incoming json from a client and creates a object of the correct type and returns it.
     * @param json
     * @return Message object with correct information
     */
    public Message parseJson(String json, String sessionid) {
        Message msg = null;
        try {
            JsonNode action = mapper.readTree(json);
            System.out.println("PARSING THIS NOW: " + action.get("action").asText());

            switch(action.get("action").asText()) {
                case "UserDoesLoginManual":{
                    msg = new ClientLogin(action.get("action").asText(),action.get("username").asText(),action.get("password").asText());
                    msg.setRecipientSessionId(action.get("recipientSessionId").asText());
                    return msg;
                }
                case "UserDoesLoginAuto":{
                    msg = new ClientLogin(action.get("action").asText(),"","");
                    msg.setRecipientSessionId(action.get("recipientSessionId").asText());
                    return msg;
                }
                case "UserDoesRegister":{
                    msg = new ClientRegister(action.get("action").asText(),action.get("username").asText(),action.get("password").asText());
                    msg.setRecipientSessionId(action.get("recipientSessionId").asText());
                    return msg;
                }
                case "UserJoinChat" : {
                    msg = new UserJoinChat(action.get("action").asText(), action.get("chatroomname").asText(),action.get("userid").asText());
                    msg.setRecipientSessionId(sessionid);
                    return msg;
                }
                case "UserSentMessage": {
                    msg = new UserSentMessage(action.get("action").asText(), action.get("userid").asText(), action.get("chatroomname").asText(), action.get("chatmessage").asText());
                    //msg.setRecipientSessionId(sessionid);
                    return msg;
                }
                case "UserLeftChatRoom":{
                    msg = new UserLeftChatRoom(action.get("action").asText(), action.get("userid").asText(), action.get("chatroomname").asText());
                    msg.setRecipientSessionId(sessionid);
                    return msg;
                }
                case "UserListChatrooms":{
                    msg = new UserListChatrooms(action.get("action").asText());
                    msg.setRecipientSessionId(sessionid);
                    return msg;
                }
                case "UserWantsUsersList":{
                    msg = new UserWantsUsersList(action.get("action").asText(), action.get("userid").asText(), action.get("searchquery").asText());
                    msg.setRecipientSessionId(sessionid);
                    return msg;
                }
                case "UserWantsToCreateGame": {
                    ArrayList<String> toInv = mapper.convertValue(action.get("toinvitedisplaynames"), ArrayList.class);
                    String[] arr = new String[toInv.size()];
                    for(int i = 0; i < toInv.size(); i++) {
                        arr[i] = toInv.get(i);
                    }
                    System.out.println("UserWantsToCreateGame size " + toInv.size());
                    msg = new UserWantsToCreateGame(action.get("action").asText(), action.get("hostid").asText(),arr);
                    return msg;
                }
                case "UserDoesGameInvitationAnswer":{
                    msg = new UserDoesGameInvitationAnswer(action.get("action").asText(), action.get("accepted").asBoolean(),action.get("userid").asText(),action.get("gameid").asText());
                    msg.setRecipientSessionId(sessionid);
                    return msg;
                }
                case "UserLeftGame":{
                    msg = new UserLeftGame(action.get("action").asText(), action.get("gameid").asText());
                    msg.setRecipientSessionId(sessionid);
                    return msg;
                }
                default: {
                    System.out.println("Unhandled incoming json: " + json);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
