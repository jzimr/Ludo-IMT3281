package no.ntnu.imt3281.ludo.logic.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.ntnu.imt3281.ludo.logic.JsonMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerThrowDice {
    JsonMessage.Actions action;
    int playerId;
    int ludoId;
    int diceRolled;
}
