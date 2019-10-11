package no.ntnu.imt3281.ludo.logic;

import no.ntnu.imt3281.ludo.Exceptions.NotEnoughPlayersException;

import java.util.Arrays;

public class Ludo {
    final static int RED = 0;
    final static int BLUE = 1;
    final static int YELLOW = 2;
    final static int GREEN = 3;

    private String[] players = new String[4];   // [0] = RED, [1] = BLUE, [2] = YELLOW, [3] = GREEN

    /**
     * Empty contructor
     */
    public Ludo(){
    }

    /**
     * Create a new Ludo game with at least 2 players (max 4 players)
     * @param user1
     * @param user2
     * @param user3
     * @param user4
     * @throws NotEnoughPlayersException
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
    }

    /**
     * Get the number of players that are active in this match
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

        return players[ID];
    }



}
