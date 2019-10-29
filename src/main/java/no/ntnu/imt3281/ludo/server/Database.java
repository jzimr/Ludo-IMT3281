package no.ntnu.imt3281.ludo.server;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;

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

    /**
     * Get the user by his ID
     *
     * @param userID the ID of the user
     * @return a data class containing all relevant info about a user
     */
    public UserInfo getUser(int userID) {
        UserInfo userInfo = null;
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user_info " +
                    "WHERE user_id = ?");
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            // loop over user
            while (rs.next()) {
                userInfo = new UserInfo(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("avatar_path"),
                        rs.getInt("games_played"),
                        rs.getInt("games_won")
                );
            }
        } catch (SQLException ex) {
            System.out.println("Error occured when trying to get user: " + ex.getMessage());
            return null;
        }

        return userInfo;
    }

    /**
     * Update a user in the database with new information
     * @param userInfo the Data class holding all relevant information about a user
     * @throws SQLException if database could not update, else none
     */
    public void updateUser(UserInfo userInfo) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE user_info " +
                "SET user_name = ?, avatar_path = ?, games_played = ?, games_won = ? " +
                "WHERE user_id = ?");

        stmt.setString(1, userInfo.getUserName());
        stmt.setString(2, userInfo.getAvatarPath());
        stmt.setInt(3, userInfo.getGamesPlayed());
        stmt.setInt(4, userInfo.getGamesWon());
        stmt.setInt(5, userInfo.getUserId());

        stmt.execute();
    }

    /**
     * Insert a new chat room into the database. Must be unique or it'll result in SQLException
     *
     * @param chatRoom the unique name of the chat room
     * @throws SQLException Exception if item could not be inserted into database
     */
    public void insertChatRoom(String chatRoom) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO chat_room" +
                "(chat_name) VALUES (?)");

        stmt.setString(1, chatRoom);
        stmt.execute();
    }

    /**
     * Get all chat rooms that are active on our server
     *
     * @return an ArrayList of all chatrooms
     */
    public ArrayList<String> getAllChatRooms() {
        ArrayList<String> chatRooms = new ArrayList<>();

        try {
            // get the messages from database
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM chat_room");

            // loop over all data and add each entry into our arraylist
            while (rs.next()) {
                chatRooms.add(rs.getString("chat_name"));
            }
        } catch (SQLException ex) {
            System.out.println("Error occured when trying to get chat rooms: " + ex.getMessage());
            return null;
        }

        return chatRooms;
    }

    /**
     * Remove a particular chatroom from the database
     * <p>
     * Removing a chatroom wil also delete all chat log entries from
     * the "chat_log" table automatically.
     * </p>
     *
     * @param chatRoom the chat room name we want to remove
     * @throws SQLException if error in deleting occurs, else none
     */
    public void removeChatRoom(String chatRoom) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM chat_room " +
                "WHERE chat_name=?");

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

        // make query ready to insert data
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO chat_log" +
                "(chat_name, user_id, chat_message, timestamp) VALUES (?, ?, ?, ?)");

        stmt.setString(1, chatName);
        stmt.setInt(2, userId);
        stmt.setString(3, chatMessage);
        stmt.setLong(4, timestamp);

        stmt.execute();
    }

    /**
     * Get all chat messages with relevant information of a particular chat room.
     *
     * @param chatName the chat room to get chat log from
     * @return Array of chat messages or null if error/none found
     */
    public ArrayList<ChatMessage> getChatMessages(String chatName) {
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();

        // get the messages
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM chat_log " +
                    "WHERE chat_name=?");
            stmt.setString(1, chatName);

            ResultSet rs = stmt.executeQuery();

            // loop over all data and add each entry into our arraylist
            while (rs.next()) {
                chatMessages.add(new ChatMessage(
                        rs.getString("chat_name"),
                        rs.getInt("user_id"),
                        rs.getString("chat_message"),
                        rs.getLong("timestamp")
                ));
            }
        } catch (SQLException ex) {
            System.out.println("Error occured when trying to get chat message: " + ex.getMessage());
            return null;
        }

        return chatMessages;
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
                // both "user_id" and "user_name" should be unique
                "PRIMARY KEY (user_id))");
    }

    /**
     * Creates the table for holding all active chats
     */
    private void createChatRoomTable() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE chat_room (" +
                "chat_name varchar(32) NOT NULL," +
                // room name should be unique
                "PRIMARY KEY (chat_name))");
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
                "FOREIGN KEY (chat_name) references chat_room(chat_name) ON DELETE CASCADE," +
                // here "user_id" is a foreign key reference to the "user_info" table
                // So that we can see the chat log of deleted users, we set the RESTRICT constraint.
                "FOREIGN KEY (user_id) references user_info(user_id) ON DELETE RESTRICT)");

    }
}
