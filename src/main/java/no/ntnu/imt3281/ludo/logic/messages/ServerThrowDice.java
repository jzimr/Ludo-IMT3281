package no.ntnu.imt3281.ludo.logic.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.ntnu.imt3281.ludo.logic.JsonMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerThrowDice {
    JsonMessage.Actions action;
    int playerId;
    int ludoId;
    int diceRolled;

    public void setAction(JsonMessage.Actions action) {
        this.action = action;
    }

    public JsonMessage.Actions getAction() {
        return action;
    }

    public void setDiceRolled(int diceRolled) {
        this.diceRolled = diceRolled;
    }

    public int getDiceRolled() {
        return diceRolled;
    }

    public void setLudoId(int ludoId) {
        this.ludoId = ludoId;
    }

    public int getLudoId() {
        return ludoId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}

