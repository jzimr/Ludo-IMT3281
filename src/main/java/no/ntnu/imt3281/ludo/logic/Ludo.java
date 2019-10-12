package no.ntnu.imt3281.ludo.logic;

import no.ntnu.imt3281.ludo.Exceptions.NoRoomForMorePlayersException;
import no.ntnu.imt3281.ludo.Exceptions.NotEnoughPlayersException;

import java.util.Arrays;

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
    private int playerTurn = 0;                     // red always starts first
    private String gameState;                  //"Created", "Initiated", "Started", "Finished"

    /**
     * Empty contructor
     */
    public Ludo(){
        // all players are "active" on start
        Arrays.fill(activePlayers, true);
        // all pieces start on local position 0
        for (int[] player: piecesPosition)
            Arrays.fill(player, 0);

        // sets the game state
        gameState = GAME_STATE_CREATED;
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

        // all players are "active" on start
        Arrays.fill(activePlayers, true);
        // all pieces start on local position 0
        for (int[] player: piecesPosition)
            Arrays.fill(player, 0);

        // sets the game state
        gameState = GAME_STATE_INITIATED;
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

        // if player not active
        if(!activePlayers[ID]){
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
        return (int)Arrays.stream(players).filter(n -> n != null && activePlayers[getPlayerID(n)]).count();
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
    String getStatus(){
        return gameState;
    }
}
