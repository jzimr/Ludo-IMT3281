package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.messages.*;

import java.io.IOException;

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
    public Message parseJson(String json) {
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
                    return msg;
                }
                case "UserSentMessage": {
                    msg = new UserSentMessage(action.get("action").asText(), action.get("userid").asText(), action.get("chatroomname").asText(), action.get("chatmessage").asText());
                    return msg;
                }
                case "UserLeftChatRoom":{
                    msg = new UserLeftChatRoom(action.get("action").asText(), action.get("userid").asText(), action.get("chatroomname").asText());
                    return msg;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
