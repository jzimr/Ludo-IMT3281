package no.ntnu.imt3281.ludo.server;

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
    private Database(String dbURL) {

        // try to establish a database connection, if not create
        try {
            connection = DriverManager.getConnection(dbURL);
        } catch (SQLException ex) {               // database is missing
            if (ex.getMessage().contains("Database") && ex.getMessage().contains("not found.")) {
                try {                            // create a new database
                    connection = DriverManager.getConnection(dbURL + ";create=true");
                    createUserInformationTable();
                    createChatRoomTable();
                    createChatLogTable();
                } catch (SQLException ex2) {      // could not create database, we exit.
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
        try {
            createUserInformationTable();
        } catch (SQLException ex) {
            if (!ex.getMessage().equals("Table/View 'USER_INFO' already exists in Schema 'APP'.")) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        try {
            createChatRoomTable();
        } catch (SQLException ex) {
            if (!ex.getMessage().equals("Table/View 'CHAT_ROOM' already exists in Schema 'APP'.")) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        try {
            createChatLogTable();
        } catch (SQLException ex) {
            if (!ex.getMessage().equals("Table/View 'CHAT_LOG' already exists in Schema 'APP'.")) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Get the database instance, or create a new one if none yet created
     *
     * @return database instance
     */
    public static Database getDatabase() {
        if (DATABASE_INSTANCE == null) {
            DATABASE_INSTANCE = new Database("jdbc:derby:./ludoDB");
        }
        return DATABASE_INSTANCE;
    }

    /**
     * Insert a new user into the database. Will fail if user already exists.
     *
     * @param userName    the name of the user
     * @param avatarPath  the image file path of the user's avatar
     * @param gamesPlayed number of total games played
     * @param gamesWon    number of total games won
     * @return true if upload was successful, else false
     */
    public void insertUser(String userName, String avatarPath, int gamesPlayed, int gamesWon) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO user_info" +
                "(user_name, avatar_path, games_played, games_won) VALUES (?, ?, ?, ?)");

        stmt.setString(1, userName);
        stmt.setString(2, avatarPath);
        stmt.setInt(3, gamesPlayed);
        stmt.setInt(4, gamesWon);

        stmt.execute();
    }

    public void getUser() {
        // todo
    }

    /**
     * Insert a new chat room into the database. Must be unique or it'll result in SQLException
     *
     * @param chatRoom the unique name of the chat room
     * @throws SQLException Exception if item could not be inserted into database
     */
    public void insertChatRoom(String chatRoom) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO chat_room" +
                "(room_name) VALUES (?)");

        stmt.setString(1, chatRoom);

        stmt.execute();
    }

    /**
     * Insert chat message into the database. It auto adds a timestamp into the database as well.
     *
     * @param chatName    The name of the user who sent the message
     * @param chatMessage The actual text the user sent to the chat message
     */
    public void insertChatMessage(String chatName, int userId, String chatMessage) throws SQLException {
        long timestamp = Instant.now().getEpochSecond();

        PreparedStatement stmt = connection.prepareStatement("INSERT INTO chat_log" +
                "(chat_name, user_id, chat_message, timestamp) VALUES (?, ?, ?, ?)");

        stmt.setString(1, chatName);
        stmt.setInt(2, userId);
        stmt.setString(3, chatMessage);
        stmt.setLong(4, timestamp);

        stmt.execute();
    }

    public String getChatLog(int chatId) {
        // todo

        return "";
    }


    /**
     * Used for setting up Database for test environment
     */
    protected static Database constructTestDatabase(String testDBURL) {
        DATABASE_INSTANCE = new Database(testDBURL);
        return DATABASE_INSTANCE;
    }

    /**
     * Creates the table for user information
     */
    private void createUserInformationTable() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE user_info (" +
                "user_id int NOT NULL GENERATED ALWAYS AS IDENTITY(START WITH 0, INCREMENT BY 1)," +
                "user_name varchar(24) NOT NULL," +
                "avatar_path varchar(120)," +
                "games_played int NOT NULL," +
                "games_won int NOT NULL," +
                "PRIMARY KEY (user_id, user_name))");            // both "user_id" and "user_name" should be unique
    }

    /**
     * Creates the table for holding all active chats
     */
    private void createChatRoomTable() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE chat_room (" +
                "room_name varchar(32) NOT NULL," +
                "PRIMARY KEY (room_name))");                  // room name should be unique
    }

    /**
     * Creates the table for the chat log
     */
    private void createChatLogTable() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE chat_log (" +
                "chat_name varchar(32) NOT NULL, " +
                "user_id int NOT NULL, " +
                "chat_message varchar(8000), " +
                "timestamp bigint," +
                // "chat_name" is a foreign key of "room_name" from table "chat_room".
                // Chat entries will be deleted if the "room_name" is deleted from table "chat_room"
                "FOREIGN KEY (chat_name) references chat_room(room_name) ON DELETE CASCADE)");
    }
}
