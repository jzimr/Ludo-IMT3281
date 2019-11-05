package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ntnu.imt3281.ludo.logic.messages.ClientLogin;
import no.ntnu.imt3281.ludo.logic.messages.ClientRegister;
import no.ntnu.imt3281.ludo.logic.messages.Message;

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

            switch(action.get("action").asText()) {
                case "UserDoesLoginManual":{
                    msg = new ClientLogin(action.get("action").asText(),action.get("username").asText(),action.get("password").asText());
                    msg.setRecipientSessionId(action.get("recipientSessionId").asText());
                    return msg;
                }
                case "UserDoesRegister":{
                    msg = new ClientRegister(action.get("action").asText(),action.get("username").asText(),action.get("password").asText());
                    msg.setRecipientSessionId(action.get("recipientSessionId").asText());
                    return msg;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
