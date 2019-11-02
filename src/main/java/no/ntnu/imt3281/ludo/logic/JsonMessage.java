package no.ntnu.imt3281.ludo.logic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



public class JsonMessage {

    //Enums containing the different actions
    public enum Actions{
        //Server Based Events
        ServerThrowDice,
        PlayerWonGame,
        PlayerStateChange,
        LoginStatus,
        RegisterStatus,

        //User Based Events
        ChatMessageEvent,
        UserJoinsChat,
        UserDoesRandomGameSearch,
        UserDoesGameInvitationAccept,
        GameInvitation,
        UserHasConnected,
        UserDoesLoginManual,
        UserDoesLoginAuto,
        UserDoesRegister,
        UserDoesPieceMove,
        UserDoesDiceThrow,

        //Test Event
        eventTest

    }

    //Always required
    Actions action; //Desired action with this message

    //UserDoesDiceThrow
    int playerId;
    int ludoId;

    int diceRolled;

    int playerWonId;

    String username;
    String password;
    Boolean loginOrRegisterStatus;


    public JsonMessage(){}

    public String getAction(){
        return action.toString();
    }

    public void setAction(Actions action) {
        this.action = action;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId(){
        return playerId;
    }

    public void setLudoId(int ludoId) {
        this.ludoId = ludoId;
    }

    public int getLudoId(){
        return ludoId;
    }

    public void setDiceRolled(int diceRolled) {
        this.diceRolled = diceRolled;
    }

    public int getDiceRolled() {
        return diceRolled;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setLoginOrRegisterStatus(Boolean loginOrRegisterStatus) {
        this.loginOrRegisterStatus = loginOrRegisterStatus;
    }

    public Boolean getLoginOrRegisterStatus() {
        return loginOrRegisterStatus;
    }

    public void setPlayerWonId(int playerWonId) {
        this.playerWonId = playerWonId;
    }

    public int getPlayerWonId() {
        return playerWonId;
    }
}

