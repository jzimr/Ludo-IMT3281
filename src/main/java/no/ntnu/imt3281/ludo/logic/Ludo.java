package no.ntnu.imt3281.ludo.logic;

import no.ntnu.imt3281.ludo.Exceptions.NoRoomForMorePlayersException;
import no.ntnu.imt3281.ludo.Exceptions.NotEnoughPlayersException;

import java.util.*;
import java.util.stream.Collectors;

public class Ludo {
    final static int RED = 0;
    final static int BLUE = 1;
    final static int YELLOW = 2;
    final static int GREEN = 3;

    final static String INACTIVE_PLAYER = "inactive";       // todo: remember to check that no player actually can register as this name

    final static String GAME_STATE_CREATED = "Created";
    final static String GAME_STATE_INITIATED = "Initiated";
    final static String GAME_STATE_STARTED = "Started";
    final static String GAME_STATE_FINISHED = "Finished";


    private String[] players = new String[4];       // [0] = RED, [1] = BLUE, [2] = YELLOW, [3] = GREEN
    private boolean[] activePlayers = new boolean[4];     // same indexing as line above. default = true
    private int[][] piecesPosition = new int[4][4];       // [playerID][pieceID] = position of piece

    private int playerTurn = 0;                         // whose turn is it? default = red
    private int timesRolled = 0;                          // times of dices rolled in a player's turn
    private int diceRolled = 0;                            // which dice the player has rolled

    private String gameState = null;                  //"Created", "Initiated", "Started", "Finished"

    private int gameWinner = -1;

    /**
     * Empty contructor
     */
    public Ludo(){
        // all players are "inactive" on start here
        Arrays.fill(activePlayers, false);
        // all pieces start on local position 0
        for (int[] player: piecesPosition)
            Arrays.fill(player, 0);

        // sets the game state
        if (gameState == null) {
            gameState = GAME_STATE_CREATED;
        }
    }

    /**
     * Create a new Ludo game with at least 2 players (max 4 players)
     * @param user1
     * @param user2
     * @param user3
     * @param user4
     * @throws NotEnoughPlayersException if less than two players
     */
    public Ludo(String user1, String user2, String user3, String user4) throws NotEnoughPlayersException {
        // if less than 2 players
        if(user1 == null || user2 == null){
            throw new NotEnoughPlayersException("At least 2 players must be connected to start a game!");
        }

        players[0] = user1;
        players[1] = user2;
        players[2] = user3;
        players[3] = user4;

        // joined players are "active" on start
        for(int i = 0; i < 4; i++){
            activePlayers[i] = players[i] != null;
        }
        // all pieces start on local position 0
        for (int[] player: piecesPosition)
            Arrays.fill(player, 0);

        // sets the game state
        if (gameState == null) {
            gameState = GAME_STATE_INITIATED;
        }
    }

    /**
     * Get the number of players in the current game
     * <p>
     *     Includes both active and inactive players
     * </p>
     * @return number of active players (1-4)
     */
    int nrOfPlayers(){
        return (int)Arrays.stream(players).filter(n -> n != null).count();
    }

    /**
     * Get the name of the Player with ID
     * @param ID ID or colour of player [RED, BLUE, YELLOW, GREEN]
     * @return the player name
     */
    String getPlayerName(int ID){
        // keep ID inside players bounds
        if(ID < 0 || ID > 3)
            return null;

        // if player exists but is not active
        if(!activePlayers[ID] && players[ID] != null){
            return "Inactive: " + players[ID];
        }

        return players[ID];
    }

    /**
     * Get the ID (1-4 or RED, BLUE, YELLOW, GREEN) of the player
     * @param playerName player name to retrieve ID of
     * @return ID of player or -1 if not found
     */
    int getPlayerID(String playerName){
        for(int i = 0; i < 4; i++){
            if(players[i] == playerName){
                return i;
            }
        }
        // if player not found
        return -1;
    }


    /**
     * Add a new player to this game
     * @param playerName name of player to add
     * @throws NoRoomForMorePlayersException if game already has a maximum of 4 players
     */
    void addPlayer(String playerName) throws NoRoomForMorePlayersException {
        // if no more room for another player
        if(nrOfPlayers() >= 4){
            throw new NoRoomForMorePlayersException("This game is full!");
        }

        activePlayers[nrOfPlayers()] = true;
        players[nrOfPlayers()] = playerName;

        //Update game state if game was created without players
        if (gameState == GAME_STATE_CREATED) {
            gameState = GAME_STATE_INITIATED;
        }

    }

    /**
     * Removes a player from the game.
     * <p>
     *     This, however, does not affect nrOfPlayers()
     * </p>
     * @param playerName name of player to remove
     */
    void removePlayer(String playerName){
        activePlayers[getPlayerID(playerName)] = false;
    }

    /**
     * Get the number of currently active players in the game
     * @return number of active players
     */
    int activePlayers(){
        // if has player and player is active
        return (int)Arrays.stream(players).filter(n -> activePlayers[getPlayerID(n)]).count();
    }

    /**
     * Get the localposition of a particular piece belonging to a player
     * @param playerID the ID of the player (0-3)
     * @param pieceID the ID of the piece (0-3)
     * @return
     */
    int getPosition(int playerID, int pieceID){
        return piecesPosition[playerID][pieceID];
    }

    /**
     * Get whose turn it is.
     * @return the playerID (0-3)
     */
    int activePlayer(){
        return playerTurn;
    }

    /**

     * Returns the state of the game
     * @return gameState
     */
    String getStatus() {
        return gameState;
    }

    /**
     * Give turn to the next player who is active
     */
    private void nextPlayerTurn(){
        for(int i = playerTurn == 3 ? 0 : playerTurn+1; i < 4; i++){
            if(activePlayers[i]){
                playerTurn = i;
                break;
            }
            if(i == 3) i = -1;
        }

        timesRolled = 0;
        diceRolled = -1;
    }

    /**
     * Throw a random die
     * @return number from the die thrown
     */
    int throwDice(){

        // Update game state.
        if (gameState == GAME_STATE_INITIATED) {
            gameState = GAME_STATE_STARTED;
        }

        Random rand = new Random();
        // get number between 1 and 6
        int randThrow = rand.nextInt(6) + 1;
        throwDice(randThrow);
        return randThrow;
    }

    /**
     * Throw a die with a specific number
     * <p>
     *     If all pieces are at home, the player will get 3 throws.
     *     If no 6 has been thrown after 3 throws, it's the next player's turn.
     * </p>
     * @param rolled the number that was rolled
     */
    int throwDice(int rolled){
        boolean allPiecesAtHome = Arrays.stream(piecesPosition[playerTurn]).allMatch(n -> n == 0);
        int piecesAtHome = (int)Arrays.stream(piecesPosition[playerTurn]).filter(n -> n == 0).count();

        timesRolled++;
        diceRolled = rolled;

        // Update game state.
        if (gameState == GAME_STATE_INITIATED) {
            gameState = GAME_STATE_STARTED;
        }


        // if all pieces at home, player gets 3 throws
        if(allPiecesAtHome) {

            // if player rolled 3 times and it's not 6 then next player's turn
            if(timesRolled == 3 && rolled != 6){
                    nextPlayerTurn();
            }

        } else {
            //Third roll cant be 6.
            if (timesRolled == 3 && diceRolled == 6) {
                nextPlayerTurn();
                return rolled;
            }

            // if we can only move 1 piece atm
            for(int i = 0; i < 4; i++){
                int piecePos = piecesPosition[playerTurn][i];
                if(piecePos+rolled > 59 && piecePos != 59){
                    nextPlayerTurn();
                    return rolled;
                }

            }

            //Skip turn if the player is blocked.
            if (towersBlocksOpponents(playerTurn, rolled)) {
                nextPlayerTurn();
            }

        }

        diceRolled = rolled;
        return diceRolled;
    }

    /**
     * Check if the player can move any pieces
     * <p>
     *     If player rolled a six, he will be able to move a piece from home -> pos 1 or to wherever else.
     *     If no move is available on any pieces AND the player did not throw a 6, it's the next player's turn.
     * </p>
     * @param playerID the ID of the player
     * @param from position to move the piece from
     * @param to position to move the piece to
     * @return if move is legal or not
     */
    boolean movePiece(int playerID, int from, int to){
        // player has not thrown dice yet
        if(diceRolled == -1){
            return false;
        }
        int pieceToBeMoved = -1;

        for(int i = 0; i < 4; i++){
            if(piecesPosition[playerID][i] == from){
                pieceToBeMoved = i; break;
            }
        }

        // if no piece can be moved, go to next player's turn
        if(pieceToBeMoved == -1){
            nextPlayerTurn();
            return false;
        }

        // move home => start position
        if(diceRolled == 6 && from == 0 && to == 1){
            piecesPosition[playerID][pieceToBeMoved] = to;
            nextPlayerTurn();
            return true;
        }

        // move over the board limit
        if(to > 59){
            nextPlayerTurn();
            return false;
        }

        // move anywhere else
        if(from + diceRolled == to){
            piecesPosition[playerID][pieceToBeMoved] = to;
            int playerUnder = getOnTopOfOtherPlayer(playerID, pieceToBeMoved);

            // check if piece ontop of another player
            if(playerUnder != -1){
                int playerPosition = getPosition(playerID, pieceToBeMoved);
                // get the specific piece that is under current player
                for(int piece = 0; piece < 4; piece++){
                    int otherPlayerPosition = getPosition(playerUnder, piece);
                    if(userGridToLudoBoardGrid(playerID, playerPosition) == userGridToLudoBoardGrid(playerUnder, otherPlayerPosition)){
                        // send him back home!
                        piecesPosition[playerUnder][piece] = 0;
                    }
                }
            }

            // if did not throw 6, go to next player
            if(diceRolled != 6){
                nextPlayerTurn();
            }

            checkIfGameIsDone();

            return true;
        }

        if(timesRolled == 3) {
            nextPlayerTurn();
            return false;
        }
        nextPlayerTurn();
        return false;
    }

    /**
     * Convert local Player position to global Board positions
     * @param playerID the id of the player
     * @param position the local player position
     * @return boardlocation or -1 if not found
     */
    int userGridToLudoBoardGrid(int playerID, int position){
        switch (playerID){
            case RED:
                return BoardLocations.RED_BOARD[position];
            case BLUE:
                return BoardLocations.BLUE_BOARD[position];
            case YELLOW:
                return BoardLocations.YELLOW_BOARD[position];
            case GREEN:
                return BoardLocations.GREEN_BOARD[position];
            default:
                return -1;
        }
    }

    /**
     * Returns the int of the player who won
     * @return Int of the winner
     */

    int getWinner(){
        return gameWinner;
    }

    /**
     * checks all pieces on the board to determine if the game is done or not.
     * It also finds out which player who won.
     */
    void checkIfGameIsDone(){

        int[] playerIds = new int[activePlayers()];
        int[] piecesDone = new int[activePlayers()];
        int playersDone = 0;

        //Get player ids of active players.
        int tempPlayerCount = 0;
        for(int i = 0; i < 4; i++) {
            if (activePlayers[i]) {
                playerIds[tempPlayerCount++] = i;
            }
        }

        //Loop over players
        for (int i = 0; i < activePlayers(); i++) {
            //Loop over pieces
            for (int j = 0; j < 4; j++) {
                if (piecesPosition[playerIds[i]][j] == 59) {
                    piecesDone[i]++;
                }
            }
        }

        //Check how many players are done
        for (int i = 0; i < activePlayers(); i++) {
            if (piecesDone[i] == 4) {
                playersDone++;
            }
        }

        //Finds the winner when the first player is done.
        if ( gameWinner == -1 && playersDone == 1) {
            int winner = Arrays.binarySearch(piecesDone, 4);
            gameWinner = winner;
        }

        //Update state when all but one players is done
        if (playersDone == activePlayers() -1) {
            gameState = GAME_STATE_FINISHED;
        }
    }

    /**
     * Get the ID of player that was stomped on.
     * @param playerID the ID of the player on top
     * @param pieceID the ID of the piece on top
     * @return ID of other player or -1 if none found
     */
    private int getOnTopOfOtherPlayer(int playerID, int pieceID){
        for(int otherPlayer = 0; otherPlayer < 4; otherPlayer++){
            for(int otherPiece = 0; otherPiece < 4; otherPiece++){
                int position = getPosition(playerID, pieceID);
                int otherPosition = getPosition(otherPlayer, otherPiece);

                // if is on same position as parameters
                if(userGridToLudoBoardGrid(playerID, position) == userGridToLudoBoardGrid(otherPlayer, otherPosition) && otherPlayer != playerID){
                    return otherPlayer;
                }
            }
        }
        return -1;
    }

    /**
     * Checks if the player is able to move.
     * @param playerID id of player
     * @param diceRolled Number that the dice rolled
     * @return true if the user is blocked, false if the player can move.
     */
    boolean towersBlocksOpponents(int playerID, int diceRolled) {

        int pieceId[] = new int[4];

        //Get pieces in play for user
        int tempPieceCount = 0;
        for(int i = 0; i < 4; i++) {
            if (piecesPosition[playerID][i] != 0) {
                pieceId[tempPieceCount++] = i;
            }
        }

        boolean moveable[] = new boolean[tempPieceCount];
        Arrays.fill(moveable, false);

        //Loop over player pieces and check if a piece are able to move.
        for(int playerPiece = 0; playerPiece < tempPieceCount; playerPiece++) {
            if (piecesPosition[playerID][pieceId[playerPiece]] != 0 &&  isPieceMoveable(playerID, pieceId[playerPiece], diceRolled)) { //user can move
                moveable[playerPiece] = true;
            }
        }

        int isThereAnyMoveable = 0;

        for (Boolean value : moveable) { //Count up how many pieces are able to move.
            if (value) {
                isThereAnyMoveable++;
            }
        }

        //Return false if there is more than one piece moveable (Not blocked), true if no pieces are moveable (Blocked)
        return !(isThereAnyMoveable > 0);
    }

    /**
     *  Checks if a selected piece if able to move
     * @param playerID id of player
     * @param pieceID Id of player piece
     * @param diceRolled Number that the dice rolled
     * @return true if the user can move, false if the user cant move.
     */
    boolean isPieceMoveable(int playerID, int pieceID, int diceRolled){

        //Get position of players piece
        int myPos = getPosition(playerID, pieceID);
        int ludoBoardPos = userGridToLudoBoardGrid(playerID, myPos) ;


        if (isThereTowers(ludoBoardPos,diceRolled)){
            return false; //Tower was found, user is blocked.
        }

        return true; //No piece was found blocking the user.
    }

    /**
     * Checks if there are any tower present on the path the user takes.
     * @param startPos Start position of piece
     * @param diceRolled Number that the dice rolled
     * @return true if there is a tower, false if not
     */
    boolean isThereTowers( int startPos, int diceRolled){

        for(int otherPlayerId = 0; otherPlayerId < 4; otherPlayerId++) {

            int[] pieceLocations = new int[4];

            for(int otherPlayerPiece = 0; otherPlayerPiece < 4; otherPlayerPiece++) {
                pieceLocations[otherPlayerPiece] = userGridToLudoBoardGrid(otherPlayerId, getPosition(otherPlayerId, otherPlayerPiece));
            }

            for(int i = 0; i < 4; i++) { //Loop over the pieceLocations array and see if we find duplicates.
                for(int j = 0; j < 4; j++) {
                    if (pieceLocations[i] == pieceLocations[j] //If we find a duplicate (Aka a tower)
                            && i != j                          //And they are not the same element
                            && pieceLocations[i] > startPos    //And the piece location are after where we are currently
                            && pieceLocations[i] <= startPos + diceRolled) { //And is lower than where we want to go.
                        return true; //Found a tower!
                    }
                }
            }

        }

        return false; //No there is no towers.
    }

}
