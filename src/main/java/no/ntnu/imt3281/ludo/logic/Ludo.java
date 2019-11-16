package no.ntnu.imt3281.ludo.logic;

import no.ntnu.imt3281.ludo.Exceptions.NoRoomForMorePlayersException;
import no.ntnu.imt3281.ludo.Exceptions.NotEnoughPlayersException;

import java.util.Arrays;
import java.util.Random;

public class Ludo {
    private String gameid;
    private String hostid;

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

    // event listeners
    private DiceListener diceListener;
    private PieceListener pieceListener;
    private PlayerListener playerListener;

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
     * Sets game id
     * @param gameid
     */
    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    /**
     * gets the game id
     * @return
     */
    public String getGameid() {
        return gameid;
    }

    /**
     * Sets the host id
     * @param hostid
     */
    public void setHostid(String hostid) {
        this.hostid = hostid;
    }

    /**
     * gets the host id
     * @return
     */
    public String getHostid() {
        return hostid;
    }

    /**
     * Get the number of players in the current game
     * <p>
     *     Includes both active and inactive players
     * </p>
     * @return number of players (1-4)
     */
    int nrOfPlayers(){
        return (int)Arrays.stream(players).filter(n -> n != null).count();
    }

    /**
     * Get the name of the Player with ID
     * @param ID ID or colour of player [RED, BLUE, YELLOW, GREEN]
     * @return the player name
     */
    public String getPlayerName(int ID){
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
    public int getPlayerID(String playerName){
        for(int i = 0; i < nrOfPlayers(); i++){
            if(players[i] != null && players[i].equals(playerName)){
                return i;
            }
        }
        // if player not found
        return -1;
    }

    /**
     * Returns all players in the game
     * @return string[] of all players
     */
    public String[] getPlayers() {
        return Arrays.stream(players).filter(player -> player != null).toArray(String[]::new);
    }

    /**
     * Returns names of all players who are active
     * @return string[] of all active players
     */
    public String[] getActivePlayers(){
        String[] arr = new String[4];

        for(int i = 0; i < getPlayers().length; i++) {
            if (activePlayers[i]) {
                arr[i] = getPlayerName(i);
            }
        }

        return Arrays.stream(arr).filter(player -> player != null).toArray(String[]::new);
    }

    /**
     * Add a new player to this game
     * @param playerName name of player to add
     * @throws NoRoomForMorePlayersException if game already has a maximum of 4 players
     */
    public void addPlayer(String playerName) throws NoRoomForMorePlayersException {
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
    public void removePlayer(String playerName){
        activePlayers[getPlayerID(playerName)] = false;
        //Send event that a user left the game.
        if (playerListener != null) {
            playerListener.playerStateChanged(new PlayerEvent(this, getPlayerID(playerName), PlayerEvent.LEFTGAME));
        }

        //if (playerTurn)
        //Skip that players turn
        nextPlayerTurn();
    }

    /**
     * Removes a player from the game.
     * <p>
     *     This however, does not affect nrOfPlayers()
     * </p>
     * @param playerId id of player to remove
     */
    public void removePlayer(int playerId) {
        activePlayers[playerId] = false;
        //Send event that a user left the game.
        if (playerListener != null) {
            playerListener.playerStateChanged(new PlayerEvent(this, playerId, PlayerEvent.LEFTGAME));
        }

        //Skip that players turn
        nextPlayerTurn();
    }

    /**
     * Get the number of currently active players in the game
     * @return number of active players
     */
    public int activePlayers(){
        // if has player and player is active
        return (int)Arrays.stream(players).filter(n -> n != null && activePlayers[getPlayerID(n)]).count();
    }

    /**
     * Get the localposition of a particular piece belonging to a player
     * @param playerID the ID of the player (0-3)
     * @param pieceID the ID of the piece (0-3)
     * @return
     */
    public int getPosition(int playerID, int pieceID){
        return piecesPosition[playerID][pieceID];
    }

    /**
     * Get whose turn it is.
     * @return the playerID (0-3)
     */
    public int activePlayer(){
        return playerTurn;
    }

    /**

     * Returns the state of the game
     * @return gameState
     */
    public String getStatus() {
        return gameState;
    }

    /**
     * Give turn to the next player who is active
     */
    private void nextPlayerTurn(){
        // call event listener
        if (playerListener != null) {
            playerListener.playerStateChanged(new PlayerEvent(this, activePlayer() ,PlayerEvent.WAITING));
        }

        for(int i = playerTurn == 3 ? 0 : playerTurn+1; i < 4; i++){
            if(activePlayers[i]){
                playerTurn = i;
                break;
            }
            if(i == 3) i = -1;
        }

        timesRolled = 0;
        diceRolled = -1;
        if (playerListener != null) {
            playerListener.playerStateChanged(new PlayerEvent(this, activePlayer() ,PlayerEvent.PLAYING));
        }
    }

    /**
     * Throw a random die
     * @return number from the die thrown
     */
    public int throwDice(){

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
    public int throwDice(int rolled){
        boolean allPiecesAtHome = Arrays.stream(piecesPosition[playerTurn]).allMatch(n -> n == 0);
        int piecesAtHome = (int)Arrays.stream(piecesPosition[playerTurn]).filter(n -> n == 0).count();

        timesRolled++;
        diceRolled = rolled;

        // call event listeners
        if(diceListener != null){
            diceListener.diceThrown(new DiceEvent(this, activePlayer(), rolled));
        }

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

            // Check if:
            // 1. pieces are at not finished AND roll will not go over 59 AND piece is not at home OR.
            // 2. piece is home and rolled six.
            // If none of the pieces can do that, we go to next player's turn.
            for(int i = 0; i < 4; i++){
                int piecePos = piecesPosition[playerTurn][i];
                if((piecePos != 59 && piecePos + rolled <= 59 && piecePos != 0) || (rolled == 6 && piecePos == 0)){
                    break;
                } else if (i == 3){
                    nextPlayerTurn();
                    return rolled;
                }
            }

            //Skip turn if the player is blocked.
            if (!towersBlocksOpponents(playerTurn, rolled)) {
                System.out.println("Towers blocks opponent, skipping turn");
                nextPlayerTurn();
                return rolled;
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
    public boolean movePiece(int playerID, int from, int to){
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
            //nextPlayerTurn();
            return false;
        }

        // move home => start position
        if(diceRolled == 6 && from == 0 && to == 1){
            piecesPosition[playerID][pieceToBeMoved] = to;
            // call event listener
            if(pieceListener != null){
                pieceListener.pieceMoved(new PieceEvent(this, playerID, pieceToBeMoved, from, to));
            }

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
                        // call event listener
                        if(pieceListener != null){
                            pieceListener.pieceMoved(new PieceEvent(this, playerUnder, piece, otherPlayerPosition, 0));
                        }
                    }
                }
            }

            nextPlayerTurn();
            return true;
        }

        // move over the board limit
        if(to > 59){
            //nextPlayerTurn();
            return false;
        }

        // move anywhere else
        if(from + diceRolled == to){
            piecesPosition[playerID][pieceToBeMoved] = to;
            int playerUnder = getOnTopOfOtherPlayer(playerID, pieceToBeMoved);

            if(pieceBlockedByTower(playerID, diceRolled, pieceToBeMoved)) {
                System.out.println("PieceBlockedByTower");
                return false;
            }

            // call event listener
            if(pieceListener != null){
                pieceListener.pieceMoved(new PieceEvent(this, playerID, pieceToBeMoved, from, to));
            }

            // check if piece ontop of another player
            if(playerUnder != -1){
                int playerPosition = getPosition(playerID, pieceToBeMoved);
                // get the specific piece that is under current player
                for(int piece = 0; piece < 4; piece++){
                    int otherPlayerPosition = getPosition(playerUnder, piece);
                    if(userGridToLudoBoardGrid(playerID, playerPosition) == userGridToLudoBoardGrid(playerUnder, otherPlayerPosition)){
                        // send him back home!
                        piecesPosition[playerUnder][piece] = 0;
                        // call event listener
                        if(pieceListener != null){
                            pieceListener.pieceMoved(new PieceEvent(this, playerUnder, piece, otherPlayerPosition, 0));
                        }
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
            //nextPlayerTurn();
            return false;
        }

        System.out.println("Vi kom hit vi- Til slutten");
        //nextPlayerTurn();
        return false;
    }

    /**
     * Convert local Player position to global Board positions
     * @param playerID the id of the player
     * @param position the local player position
     * @return boardlocation or -1 if not found
     */
    public int userGridToLudoBoardGrid(int playerID, int position){
        switch (playerID){
            case RED:
                return LocalToGlobalBoard.RED_BOARD[position];
            case BLUE:
                return LocalToGlobalBoard.BLUE_BOARD[position];
            case YELLOW:
                return LocalToGlobalBoard.YELLOW_BOARD[position];
            case GREEN:
                return LocalToGlobalBoard.GREEN_BOARD[position];
            default:
                return -1;
        }
    }

    /**
     * Returns the int of the player who won
     * @return Int of the winner
     */

    public int getWinner(){
        return gameWinner;
    }

    /**
     * checks all pieces on the board to determine if the game is done or not.
     * It also finds out which player who won.
     */
    public void checkIfGameIsDone(){

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

            //Send event with game winner.
            if (playerListener != null) {
                playerListener.playerStateChanged(new PlayerEvent(this, winner, PlayerEvent.WON));
            }
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
     * @return false if the user is blocked, true if the player can move.
     */
    private boolean towersBlocksOpponents(int playerID, int diceRolled) {

        for (int i = 0; i < 4; i++){ //Playerid

            for (int j = 0; j < 4; j++){ //Pieceid
                for(int k = 0; k < 4; k++){//Pieceid

                    int pieceid1_local = getPosition(i,j);
                    int pieceid2_local = getPosition(i,k);

                    int pieceid1 = userGridToLudoBoardGrid(i,getPosition(i,j));
                    int pieceid2 = userGridToLudoBoardGrid(i,getPosition(i,k));

                    if(pieceid1 == pieceid2 && j != k && pieceid1_local != 0 && pieceid2_local != 0 && playerID != i){ //any towers?
                        for (int l = 0; l < 4; l++){ // My pieces
                            int myPiece = userGridToLudoBoardGrid(playerID,getPosition(playerID,l));
                                /*if (pieceid1 > myPiece + diceRolled && myPiece != 0){ //Not blocking
                                    return false;
                                }*/

                            //if (pieceid1 < myPiece+diceRolled && diceRolled == 6) { //In the way but we can go over.
                            //return true;
                            //}

                            if (pieceid1 <= myPiece + diceRolled && ((pieceid1 - myPiece) < 6) && (pieceid1 - myPiece > 0)) { //Blocked, cant land on top or after a piece.
                                for (int x = 0; x < 4; x++) { // Check if other pieces are in play.
                                    int myOtherPiece = userGridToLudoBoardGrid(playerID,getPosition(playerID,x));
                                    int myOtherPieceLocal = getPosition(playerID,x);
                                    if ((myPiece != myOtherPiece && myOtherPieceLocal != 0)) {
                                        //Other pieces are available. Return true;
                                        System.out.println("hei");
                                        return true;
                                    }
                                }
                                System.out.println("Nei for faen");

                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean pieceBlockedByTower(int playerID, int diceRolled, int pieceToBeMoved) {
        for (int i = 0; i < 4; i++){ //Playerid

            for (int j = 0; j < 4; j++){ //Pieceid
                for(int k = 0; k < 4; k++){//Pieceid

                    int pieceid1_local = getPosition(i,j);
                    int pieceid2_local = getPosition(i,k);

                    int pieceid1 = userGridToLudoBoardGrid(i,getPosition(i,j));
                    int pieceid2 = userGridToLudoBoardGrid(i,getPosition(i,k));

                    if(pieceid1 == pieceid2 && j != k && pieceid1_local != 0 && pieceid2_local != 0 && playerID != i){ //any towers?
                        int myPiece = userGridToLudoBoardGrid(playerID,getPosition(playerID,pieceToBeMoved));

                        if (pieceid1 < myPiece+diceRolled && diceRolled == 6) { //In the way but we can go over.
                            System.out.println("uhm");
                            return false;
                        }

                        System.out.println("we in boys");
                        for(int playerMove = 0; playerMove != diceRolled; playerMove++) {
                            System.out.println("We in boys: " + playerMove + ", " + diceRolled + ", " + myPiece + ", " + pieceid1);
                            if (myPiece + playerMove == pieceid1) {
                                System.out.println("HEI");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }



    /**
     * Add the Dice listener to this particular Ludo class
     * @param diceListener the Dice listener object we want to send events to
     */
    public void addDiceListener(DiceListener diceListener){
        this.diceListener = diceListener;
    }

    public void addPieceListener(PieceListener pieceListener){
        this.pieceListener = pieceListener;
    }

    public void addPlayerListener(PlayerListener playerlistener){
        this.playerListener = playerlistener;
    }
}