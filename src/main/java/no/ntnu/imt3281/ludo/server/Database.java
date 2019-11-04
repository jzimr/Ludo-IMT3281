package no.ntnu.imt3281.ludo.server;

import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import no.ntnu.imt3281.ludo.logic.SHA512Hasher;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

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
                    createLoginInformationTable();
                    createUserInformationTable();
                    createSessionInformationTable();
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
            createLoginInformationTable();
            createUserInformationTable();
            createSessionInformationTable();
            createChatRoomTable();
            createChatLogTable();
        } catch (SQLException ex) {
            // if error is something else than that the table already exists
            if (!ex.getMessage().equals("Table/View 'LOGIN_INFO' already exists in Schema 'APP'.")
                    && !ex.getMessage().equals("Table/View 'SESSION_INFO' already exists in Schema 'APP'.")
                    && !ex.getMessage().equals("Table/View 'USER_INFO' already exists in Schema 'APP'.")
                    && !ex.getMessage().equals("Table/View 'CHAT_ROOM' already exists in Schema 'APP'.")
                    && !ex.getMessage().equals("Table/View 'CHAT_LOG' already exists in Schema 'APP'.")) {
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
     * Insert a new account for the user. Password will be hashed here before it is inserted into the database.
     * <p>
     * Will also automatically create a new profile for the user
     * by calling "insertUser"
     * </p>
     *
     * @param accountName the login username of the user
     * @param password the password in plain text.
     */
    public void insertAccount(String accountName, String password) throws SQLException {
        SHA512Hasher hasher = new SHA512Hasher();
        // generate a random ID for our user
        String uniqueId = UUID.randomUUID().toString();

        // generate a random salt for this account
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        // hash user's password before we insert into database
        String hashedPwd = hasher.hash(password, salt);

        // first insert a new account into our "login_info" table
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO login_info" +
                "(USER_ID, ACCOUNT_NAME, PWD_HSH, ACCOUNT_SALT) VALUES (?, ?, ?, ?)");

        stmt.setString(1, uniqueId);
        stmt.setString(2, accountName);
        stmt.setString(3, hashedPwd);
        stmt.setBytes(4, salt);
        stmt.execute();

        // at last insert a new user so he has a profile when he logs in for the first time
        insertProfile(uniqueId, accountName, "", 0, 0);
    }

    /**
     * Check if account name is already taken.
     * <p>
     *     No-one can have an account name that is already taken.
     * </p>
     * @param nameToCheck the account name or display name we want to check
     * @return if the name is available
     */
    public boolean doesAccountNameExist(String nameToCheck) throws SQLException{
        // create connection to database
        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(account_name) AS name_count FROM login_info " +
                "WHERE account_name = ?");
        stmt.setString(1, nameToCheck);
        ResultSet rs = stmt.executeQuery();

        rs.next();
        // get the total count of account names (1 if it exists, 0 else)
        int count = rs.getInt("name_count");
        rs.close();

        return count == 0 ? false : true;
    }

    /**
     * Check if the user-entered login values match the values in the database.
     * <p>
     *     We check by comparing the sent hashed password with the hashed value in our
     *     database using the SHA512Hasher class.
     * </p>
     * @param accountName the login name of the account
     * @param password the hashed plaintext password to compare to. DO NOT SEND PLAIN PASSWORD! HASH FIRST!
     * @return if the login matches with values in our database or not
     * @throws SQLException if error occured in database
     */
    public boolean checkIfLoginValid(String accountName, String password) throws SQLException{
        SHA512Hasher hasher = new SHA512Hasher();
        String pwd_hsh = "", account_name = "";
        byte[] salt = null;

        // create connection to database
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM login_info " +
                "WHERE account_name = ?");
        stmt.setString(1, accountName);
        ResultSet rs = stmt.executeQuery();

        // get hashed password from database that's linked to this user
        while (rs.next()) {
            pwd_hsh = rs.getString("pwd_hsh");
            account_name = rs.getString("account_name");
            salt = rs.getBytes("account_salt");
        }

        // if password or username was not found in database
        if(pwd_hsh.equals("") || account_name.equals(""))
            return false;

        // check if entered password and username matches entries in database
        return account_name.equals(accountName) && hasher.checkHashedValue(pwd_hsh, password, salt);
    }

    /**
     * Check if the sessionId is to be found in our database.
     * <p>
     *     We check by searching for occurences of the entered "sessionId" in the database.
     *     If none found, the token is not valid.
     * </p>
     * @param sessionId the ID of the session user tries to log in with
     * @return if the login matches with values in our database or not
     * @throws SQLException if error occured in database
     */
    public boolean checkIfLoginValid(String sessionId) throws SQLException{
        // create connection to database
        PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) AS session_count FROM session_info " +
                "WHERE session_id = ?");
        stmt.setString(1, sessionId);
        ResultSet rs = stmt.executeQuery();

        rs.next();
        // get the total count of session_info names (1 if it exists, 0 else)
        int count = rs.getInt("session_count");
        rs.close();

        // check if sessionID is valid
        return count == 0 ? false : true;
    }

    /**
     * Insert a new session for the particular user.
     * @param sessionId the session of the user.
     * @param userId the ID of the user.
     * @throws SQLException if error occured in database
     */
    public void insertSessionToken(String sessionId, String userId) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO session_info" +
                "(session_id, user_id) VALUES (?, ?)");

        stmt.setString(1, sessionId);
        stmt.setString(2, userId);
        stmt.execute();
    }

    /**
     * Remove a session token from a particular user
     * @param userId the ID of the user to remove token from
     * @throws SQLException if error occured in database
     */
    public void terminateSessionToken(String userId) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM session_info " +
                "WHERE user_id = ?");

        stmt.setString(1, userId);
        stmt.execute();
    }

    /**
     *
     * @param sessionToken
     * @throws SQLException
     */
    public String getUserId(String sessionToken){
        String userId = "";
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT user_id FROM session_info " +
                    "WHERE session_id = ?");
            stmt.setString(1, sessionToken);
            ResultSet rs = stmt.executeQuery();

            rs.next();
            userId = rs.getString("user_id");
            rs.close();
        } catch (SQLException ex) {
            System.out.println("Error occured when trying to get user: " + ex.getMessage());
            return "";
        }

        return userId;
    }

    /**
     * Update the password of the account for a given user
     * @param userId the unique ID of the user
     * @param newHashedPwd the hashed password. DO NOT SEND PLAIN PASSWORD! HASH FIRST!
     */
    public void updateAccountPassword(String userId, String newHashedPwd) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE login_info " +
                "SET pwd_hsh = ? " +
                "WHERE user_id = ?");

        stmt.setString(1, newHashedPwd);
        stmt.setString(2, userId);

        stmt.execute();
    }

    /**
     * Insert a new user into the database. Will fail if user already exists.
     * <p>
     * Must first have an account for insertion to work.
     * </p>
     *
     * @param userId the unique ID of the user
     * @param displayName the name of the user
     * @param avatarPath  the image file path of the user's avatar
     * @param gamesPlayed number of total games played
     * @param gamesWon    number of total games won
     * @return true if upload was successful, else false
     */
    protected void insertProfile(String userId, String displayName, String avatarPath, int gamesPlayed, int gamesWon) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO user_info" +
                "(user_id, display_name, avatar_path, games_played, games_won) VALUES (?, ?, ?, ?, ?)");

        stmt.setString(1, userId);
        stmt.setString(2, displayName);
        stmt.setString(3, avatarPath);
        stmt.setInt(4, gamesPlayed);
        stmt.setInt(5, gamesWon);

        stmt.execute();
    }

    /**
     * Get the user by his account name
     *
     * @param userId the user ID of the user
     * @return a data class containing all relevant info about a user
     */
    public UserInfo getProfile(String userId) {
        UserInfo userInfo = null;
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user_info " +
                    "WHERE user_id = ?");
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            // loop over user
            while (rs.next()) {
                userInfo = new UserInfo(
                        rs.getString("user_id"),
                        rs.getString("display_name"),
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
     *
     * @param userInfo the Data class holding all relevant information about a user
     * @throws SQLException if database could not update, else none
     */
    public void updateProfile(UserInfo userInfo) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE user_info " +
                "SET display_name = ?, avatar_path = ?, games_played = ?, games_won = ? " +
                "WHERE user_id = ?");

        stmt.setString(1, userInfo.getDisplayName());
        stmt.setString(2, userInfo.getAvatarPath());
        stmt.setInt(3, userInfo.getGamesPlayed());
        stmt.setInt(4, userInfo.getGamesWon());
        stmt.setString(5, userInfo.getUserId());

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
    public void insertChatMessage(String chatName, String userId, String chatMessage) throws SQLException {
        long timestamp = Instant.now().getEpochSecond();

        // make query ready to insert data
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO chat_log" +
                "(chat_name, user_id, chat_message, timestamp) VALUES (?, ?, ?, ?)");

        stmt.setString(1, chatName);
        stmt.setString(2, userId);
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
                        rs.getString("user_id"),
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
     * Creates the table for login information (login user_name and hashed password + its salt)
     *
     * @throws SQLException if table could not be created, else none
     */
    private void createLoginInformationTable() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE login_info (" +
                "user_id varchar(36) NOT NULL," +
                "account_name varchar(24) NOT NULL," +
                "pwd_hsh varchar(128) NOT NULL," +
                "account_salt char(16) FOR BIT DATA NOT NULL," +
                // "user_id" should be unique and primary key
                "PRIMARY KEY (user_id)," +
                // "user_name" should be unique
                "UNIQUE (account_name))");
    }

    /**
     * Creates the table for user information
     *
     * @throws SQLException if table could not be created, else none
     */
    private void createUserInformationTable() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE user_info (" +
                "user_id varchar(36) NOT NULL," +
                "display_name varchar(24) NOT NULL," +
                "avatar_path varchar(120)," +
                "games_played int NOT NULL," +
                "games_won int NOT NULL," +
                // "user_id" should be unique and primary key
                "PRIMARY KEY (user_id)," +
                // "display_name" should be unique
                "UNIQUE (display_name)," +
                // "user_id" is a foreign key of "user_id" from table "login_info".
                // We set the RESTRICT constraint, since users should never be completely deleted
                "FOREIGN KEY (user_id) references login_info(user_id) ON DELETE RESTRICT)");
    }

    /**
     * Create the table for session tokens
     * @throws SQLException if table could not be created, else none
     */
    private void createSessionInformationTable() throws SQLException{
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE session_info (" +
                "session_id varchar(36) NOT NULL," +
                "user_id varchar(36) NOT NULL," +
                // "session_id" should be unique and primary key
                "PRIMARY KEY (session_id)," +
                "UNIQUE (user_id)," +
                // ON DELETE CASCADE because no point in having a session if user does not exist
                "FOREIGN KEY (user_id) references login_info(user_id) ON DELETE CASCADE)");
    }

    /**
     * Creates the table for holding all active chats
     *
     * @throws SQLException if table could not be created, else none
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
     *
     * @throws SQLException if table could not be created, else none
     */
    private void createChatLogTable() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE chat_log (" +
                "chat_name varchar(32) NOT NULL, " +
                "user_id varchar(36) NOT NULL," +
                "chat_message varchar(8000), " +
                "timestamp bigint," +
                // "chat_name" is a foreign key of "room_name" from table "chat_room".
                // Chat entries will be deleted if the "room_name" is deleted from table "chat_room"
                "FOREIGN KEY (chat_name) references chat_room(chat_name) ON DELETE CASCADE," +
                // here "user_id" is a foreign key reference to the "user_info" table
                // So that we can see the chat log of deleted users, we set the RESTRICT constraint.
                "FOREIGN KEY (user_id) references login_info(user_id) ON DELETE RESTRICT)");
    }
}