package no.ntnu.imt3281.ludo.server;

import org.apache.derby.iapi.services.monitor.DerbyObserver;

import java.sql.*;
import java.time.Instant;

/**
 * Singleton Database class
 */
public class Database {
    private static Database DATABASE_INSTANCE = null;
    private Connection connection = null;

    /**
     * setup database
     */
    private Database(String dbURL){

        // try to establish a database connection, if not create
        try{
            connection = DriverManager.getConnection(dbURL);
        } catch(SQLException ex){               // database is missing
            if(ex.getMessage().contains("Database") && ex.getMessage().contains("not found.")){
                try{                            // create a new database
                    connection = DriverManager.getConnection(dbURL + ";create=true");
                    createUserInformationTable();
                    createChatLogTable();
                } catch(SQLException ex2){      // could not create database, we exit.
                    System.err.println("Could not create database.");
                    ex.printStackTrace();
                    System.exit(1);
                }
            } else {                            // Database exists, but could not connect for some reason, we exit.
                System.err.println("Could not connect to database. " + ex.getMessage());
                System.exit(1);
            }
        }

        // check that tables exist (in case they have been DROP'ed)
        try{
            createUserInformationTable();
        } catch(SQLException ex){
            if(!ex.getMessage().equals("Table/View 'USER_INFO' already exists in Schema 'APP'.")){
                ex.printStackTrace();
                System.exit(1);
            }
        }
        try{
            createChatLogTable();
        } catch(SQLException ex){
            if(!ex.getMessage().equals("Table/View 'CHAT_LOG' already exists in Schema 'APP'.")){
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Get the database instance, or create a new one if none yet created
     * @return database instance
     */
    public static Database getDatabase(){
        if(DATABASE_INSTANCE == null){
            DATABASE_INSTANCE = new Database("jdbc:derby:./ludoDB");
        }
        return DATABASE_INSTANCE;
    }

    /**
     * Insert a new user into the database. Will fail if user already exists.
     * @param userName the name of the user
     * @param avatarPath the image file path of the user's avatar
     * @param gamesPlayed number of total games played
     * @param gamesWon number of total games won
     * @return true if upload was successful, else false
     */
    public boolean insertUser(String userName, String avatarPath, int gamesPlayed, int gamesWon){
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO user_info" +
                    "(user_name, avatar_path, games_played, games_won) VALUES (?, ?, ?, ?)");

            stmt.setString(1, userName);
            stmt.setString(2, avatarPath);
            stmt.setInt(3, gamesPlayed);
            stmt.setInt(4, gamesWon);

            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void getUser(){
        // todo
    }

    /**
     * Insert chat message into the database. It auto adds a timestamp into the database as well.
     * @param chatId Id of chat room the message was sent in
     * @param chatName The name of the user who sent the message
     * @param chatMessage The actual text the user sent to the chat message
     */
    public void insertChatMessage(int chatId, String chatName, String chatMessage){
        try {

            Statement stmt = connection.createStatement();
            long timestamp = Instant.now().getEpochSecond();

            String query = "INSERT INTO chat_log("
                    + "chat_id, chat_name, chat_message, timestamp) VALUES "
                    + "(" + chatId + ",'"
                    + chatName + "', '"+
                    chatMessage +"',"
                    + timestamp +") ";

            stmt.execute(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getChatLog(int chatId){
        // todo

        return "";
    }



    /**
     * Used for setting up Database for test environment
     */
    protected static Database constructTestDatabase(String testDBURL){
        DATABASE_INSTANCE = new Database(testDBURL);
        return DATABASE_INSTANCE;
    }

    /**
     * Creates the table for user information
     */
    private void createUserInformationTable() throws SQLException{
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE user_info (" +
                "user_id int NOT NULL GENERATED ALWAYS AS IDENTITY(START WITH 0, INCREMENT BY 1)," +
                "user_name varchar(24) NOT NULL," +
                "avatar_path varchar(120)," +
                "games_played int NOT NULL," +
                "games_won int NOT NULL)");

        // both "user_id" and "user_name" should be unique
        stmt.execute("CREATE UNIQUE INDEX user_idx on user_info(user_id)");
        stmt.execute("CREATE UNIQUE INDEX user_namex on user_info(user_name)");
    }

    /**
     * Creates the table for the chat log
     */
    private void createChatLogTable() throws SQLException{
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE chat_log (" +
                "chat_id int NOT NULL," +
                "chat_name varchar(32) NOT NULL," +
                "chat_message varchar(8000)," + //Todo: Change varchar length to something else than 8000
                "timestamp bigint)");
    }
}
