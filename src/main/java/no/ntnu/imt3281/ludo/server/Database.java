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
        try{                                    // try to connect to database
            connection = DriverManager.getConnection(dbURL);
        } catch(SQLException ex){               // database is missing
            if(ex.getMessage().contains("Database") && ex.getMessage().contains("not found.")){
                try{                            // create a new database
                    connection = DriverManager.getConnection(dbURL + ";create=true");
                    createUserInformationTable();
                    createChatLogTable();
                } catch(SQLException ex2){      // could not create database, we exit.
                    System.err.println("Could not create database. " + ex.getMessage());
                    System.exit(1);
                }
            } else {                            // Database exists, but could not connect for some reason, we exit.
                System.err.println("Could not connect to database. " + ex.getMessage());
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

    public void insertUser(){
        // todo
    }

    public void getUser(){
        // todo
    }

    /**
     * Insert chat message into the database. It auto adds a timestamp into the database as well.
     * @param chatName The name of the user who sent the message
     * @param chatMessage The actual text the user sent to the chat message
     */
    public void insertChatMessage(int chatId, String chatName, int userId, String chatMessage) throws SQLException{

            Statement stmt = connection.createStatement();
            long timestamp = Instant.now().getEpochSecond();

            String query = "INSERT INTO chat_log("
                    + "chat_id, chat_name, user_id, chat_message, timestamp) VALUES "
                    + "("
                    + chatId + ",'"
                    + chatName + "', "
                    + userId + ",'"
                    +chatMessage +"',"
                    + timestamp +") ";

            stmt.execute(query);

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
    private void createUserInformationTable(){
        try{
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
        } catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Creates the table for the chat log
     */
    private void createChatLogTable(){
        try{
            Statement stmt = connection.createStatement();

            stmt.execute("CREATE TABLE chat_log (chat_id int NOT NULL, chat_name varchar(32) NOT NULL, user_id int NOT NULL, chat_message varchar(8000), timestamp bigint)");
        } catch(SQLException ex){
            ex.printStackTrace();
        }
    }


}
