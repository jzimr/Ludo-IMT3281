package no.ntnu.imt3281.ludo.logic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonMessage {

    //Enums containing the different actions
    enum Actions{
        //Server Based Events
        ServerThrowDice,
        PlayerWonGame,
        PlayerStateChange,

        //User Based Events
        ChatMessageEvent,
        UserJoinsChat,
        UserDoesRandomGameSearch,
        UserDoesGameInvitationAccept,
        GameInvitation,
        UserHasConnected,
        UserDoesLoginOrRegister,
        UserDoesPieceMove,
        UserDoesDiceThrow

    }

    //Always required
    Actions action; //Desired action with this message

    //UserDoesDiceThrow
    int playerId;
    int ludoId;

    public JsonMessage(){}

    public String getAction(){
        return action.toString();
    }

    public int getPlayerId(){
        return playerId;
    }

    public int getLudoId(){
        return ludoId;
    }


}

