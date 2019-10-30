package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.logic.PBKDF2Hasher;
import org.junit.*;

import java.sql.*;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class DatabaseTest {
    private static final String dbURL = "jdbc:derby:./ludoTestDB";
    private static Database testDatabase;       // our test database
    private static Connection testConnection;          // database connection to execute sql statements
    private static PBKDF2Hasher hasher = new PBKDF2Hasher();    // our hasher object for hashing passwords

    /**
     * Sets up database before a new test is run
     */
    @Before
    public void setupDatabase() {
        // create test database instance
        try {
            testDatabase = Database.constructTestDatabase(dbURL);
        } catch (Exception ex) {
            assertFalse(true);
        }

        // we are supposed to get the connection
        try {
            testConnection = DriverManager.getConnection(dbURL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // fail test if not
            assertFalse(true);
        }
    }

    /**
     * Remove all data after each test (including DROPing tables)
     */
    @After
    public void purgeDatabaseAtEndOfTests() {
        // remove all data from records
        try {
            Statement statement = testConnection.createStatement();
            statement.execute("DROP TABLE chat_log");
            statement.execute("DROP TABLE chat_room");
            statement.execute("DROP TABLE user_info");
            statement.execute("DROP TABLE login_info");
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    /**
     * Tests that our database was correctly setup with the desired tables and columns
     */
    @Test
    public void initialTest() {
        Statement statement;
        ResultSet resultSet = null;

        // test login_info table
        try {
            statement = testConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM login_info");
        } catch (SQLException ex) {
            assertFalse(true);
        }

        // check if tables have all the required columns and only has 5 columns
        try {
            assertEquals("USER_ID", resultSet.getMetaData().getColumnName(1));
            assertEquals("ACCOUNT_NAME", resultSet.getMetaData().getColumnName(2));
            assertEquals("PWD_HSH", resultSet.getMetaData().getColumnName(3));
            assertEquals(3, resultSet.getMetaData().getColumnCount());
        } catch (SQLException ex) {
            assertFalse(true);
        }

        // test user_info table
        try {
            statement = testConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM user_info");
        } catch (SQLException ex) {
            assertFalse(true);
        }

        // check if tables have all the required columns and only has 5 columns
        try {
            assertEquals("USER_ID", resultSet.getMetaData().getColumnName(1));
            assertEquals("DISPLAY_NAME", resultSet.getMetaData().getColumnName(2));
            assertEquals("AVATAR_PATH", resultSet.getMetaData().getColumnName(3));
            assertEquals("GAMES_PLAYED", resultSet.getMetaData().getColumnName(4));
            assertEquals("GAMES_WON", resultSet.getMetaData().getColumnName(5));
            assertEquals(5, resultSet.getMetaData().getColumnCount());
        } catch (SQLException ex) {
            assertFalse(true);
        }

        // test chat_log table
        try {
            statement = testConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM chat_log");
        } catch (SQLException ex) {
            assertFalse(true);
        }

        // check if tables have all the required columns and only has 4 columns
        try {
            assertEquals("CHAT_NAME", resultSet.getMetaData().getColumnName(1));
            assertEquals("USER_ID", resultSet.getMetaData().getColumnName(2));
            assertEquals("CHAT_MESSAGE", resultSet.getMetaData().getColumnName(3));
            assertEquals("TIMESTAMP", resultSet.getMetaData().getColumnName(4));
            assertEquals(4, resultSet.getMetaData().getColumnCount());
        } catch (SQLException ex) {
            assertFalse(true);
        }

        // test chat_room table
        try {
            statement = testConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM chat_room");
        } catch (SQLException ex) {
            assertFalse(true);
        }

        // check if tables have all the required columns and only has 1 columns
        try {
            assertEquals("CHAT_NAME", resultSet.getMetaData().getColumnName(1));
            assertEquals(1, resultSet.getMetaData().getColumnCount());
        } catch (SQLException ex) {
            assertFalse(true);
        }
    }

    /**
     * Helper function to insert two new accounts into the database
     * which will also automatically insert two new users (profiles)
     */
    private void insertTwoAccounts(){
        try {
            // Boby's and Samy's passwords
            String pwd1 = "BobysFavoriteDog123",
                    pwd2 = "SamysMotherMaidenName";

            // hash our passwords
            pwd1 = hasher.hash(pwd1.toCharArray());
            pwd2 = hasher.hash(pwd2.toCharArray());

            // insert into database
            testDatabase.insertAccount("Boby", pwd1);
            testDatabase.insertAccount("Samy", pwd2);

            // get the profile information
            UserInfo user1 = testDatabase.getProfile(0);
            UserInfo user2 = testDatabase.getProfile(1);

            // change info for both users
            user1.setDisplayName("Boby");
            user1.setAvatarPath("someImage.png");
            user1.setGamesPlayed(10);
            user1.setGamesWon(3);

            user2.setDisplayName("Samy");
            user2.setAvatarPath("someOtherImage.png");
            user2.setGamesPlayed(6);
            user2.setGamesWon(6);

            // update data for both users in database
            testDatabase.updateProfile(user1);
            testDatabase.updateProfile(user2);
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Helper function to insert two chatrooms into the databasee
     */
    private void insertTwoChatRooms() {
        //Try to insert chat room into db and check if the data is correct.
        try {
            testDatabase.insertChatRoom("Testroom");
            testDatabase.insertChatRoom("Testroom2");
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Helper function to insert two messages into the database (REQUIRES 2 users minimum first!)
     */
    private void insertTwoMessages() {
        try {
            testDatabase.insertChatMessage("Testroom", 0, "Wow, what a great game :)");
            testDatabase.insertChatMessage("Testroom2", 1, "Ye, this game deserves an 'A'!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test if we successfully can insert two new accounts into the database
     */
    @Test
    public void insertAccountTest(){
        // insert two users
        insertTwoAccounts();
        // Boby's and Samy's passwords
        String pwd1 = "BobysFavoriteDog123",
                pwd2 = "SamysMotherMaidenName";

        try {
            Statement state = testConnection.createStatement();

            // execute SELECT query
            ResultSet rs = state.executeQuery("SELECT * FROM login_info WHERE user_id=0");

            // loop over data of user 1
            while (rs.next()) {
                assertEquals(0, rs.getInt("user_id"));
                assertEquals("Boby", rs.getString("account_name"));
                // check that the passwords are correct using the BKDF2Hasher class
                assertTrue(hasher.checkPassword(pwd1.toCharArray(), rs.getString("pwd_hsh")));
            }

            // execute SELECT query
            rs = state.executeQuery("SELECT * FROM login_info WHERE user_id=1");

            // loop over data of user 1
            while (rs.next()) {
                assertEquals(1, rs.getInt("user_id"));
                assertEquals("Samy", rs.getString("account_name"));
                // check that the passwords are correct using the BKDF2Hasher class
                assertTrue(hasher.checkPassword(pwd2.toCharArray(), rs.getString("pwd_hsh")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Tets if we can correctly authenticate users trying to log in with both correct
     * and wrong usernames/passwords
     */
    @Test
    public void checkIfLoginValidTest(){
        // insert two users
        insertTwoAccounts();
        // Boby's and Samy's passwords
        String pwd1 = "BobysFavoriteDog123",
                pwd2 = "SamysMotherMaidenName";

        try{
            // correct paswords + usernames
            assertTrue(testDatabase.checkIfLoginValid(hasher, "Boby", pwd1.toCharArray()));
            assertTrue(testDatabase.checkIfLoginValid(hasher, "Samy", pwd2.toCharArray()));

            // wrong usernames
            assertFalse(testDatabase.checkIfLoginValid(hasher, "Boby1", pwd1.toCharArray()));
            assertFalse(testDatabase.checkIfLoginValid(hasher, "Samyy", pwd2.toCharArray()));

            // wrong passwords
            pwd1 += "s";
            pwd2.substring(0, pwd2.length()-2);
            assertFalse(testDatabase.checkIfLoginValid(hasher, "Boby1", pwd1.toCharArray()));
            assertFalse(testDatabase.checkIfLoginValid(hasher, "Samyy", pwd2.toCharArray()));
        } catch(SQLException ex){
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test if we can successfully change the password in our account
     */
    @Test
    public void updateAccountPasswordTest(){
        // insert two users
        insertTwoAccounts();

        // Boby's and Samy's NEW passwords
        String pwd1 = "BobyLostHisDog",
                pwd2 = "SamyLostHisMother";

        try{
            // first update password in database with new hashed passwords
            testDatabase.updateAccountPassword(0, hasher.hash(pwd1.toCharArray()));
            testDatabase.updateAccountPassword(1, hasher.hash(pwd2.toCharArray()));

            // then check if they match
            Statement state = testConnection.createStatement();

            // execute SELECT query
            ResultSet rs = state.executeQuery("SELECT * FROM login_info WHERE user_id=0");

            // loop over data of user 1
            while (rs.next()) {
                assertEquals(0, rs.getInt("user_id"));
                assertEquals("Boby", rs.getString("account_name"));
                // check that the passwords are correct using the BKDF2Hasher class
                assertTrue(hasher.checkPassword(pwd1.toCharArray(), rs.getString("pwd_hsh")));
            }

            // execute SELECT query
            rs = state.executeQuery("SELECT * FROM login_info WHERE user_id=1");

            // loop over data of user 2
            while (rs.next()) {
                assertEquals(1, rs.getInt("user_id"));
                assertEquals("Samy", rs.getString("account_name"));
                // check that the passwords are correct using the BKDF2Hasher class
                assertTrue(hasher.checkPassword(pwd2.toCharArray(), rs.getString("pwd_hsh")));
            }

        } catch(SQLException ex){
            ex.printStackTrace();
            assertTrue(false);
        }
    }



    /**
     * Testing if we successfully add and can retrieve 2 users in the database
     * It also tests if the database auto increments the unique ID of the users
     */
    @Test
    public void insertProfileTest() {
        // insert two users
        insertTwoAccounts();

        try {
            Statement state = testConnection.createStatement();

            // execute SELECT query
            ResultSet rs = state.executeQuery("SELECT * FROM user_info WHERE user_id=0");

            // loop over data of user 1
            while (rs.next()) {
                assertEquals(0, rs.getInt("user_id"));
                assertEquals("Boby", rs.getString("display_name"));
                assertEquals("someImage.png", rs.getString("avatar_path"));
                assertEquals(10, rs.getInt("games_played"));
                assertEquals(3, rs.getInt("games_won"));
            }

            // execute SELECT query
            rs = state.executeQuery("SELECT * FROM user_info WHERE user_id=1");

            // loop over data of user 2
            while (rs.next()) {
                assertEquals(1, rs.getInt("user_id"));
                assertEquals("Samy", rs.getString("display_name"));
                assertEquals("someOtherImage.png", rs.getString("avatar_path"));
                assertEquals(6, rs.getInt("games_played"));
                assertEquals(6, rs.getInt("games_won"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test if we get the two users we insert into the database
     */
    @Test
    public void getProfileTest() {
        // insert two users
        insertTwoAccounts();

        // get both users
        UserInfo user1 = testDatabase.getProfile(0);
        UserInfo user2 = testDatabase.getProfile(1);

        // compare data of user1
        assertEquals(0, user1.getUserId());
        assertEquals("Boby", user1.getDisplayName());
        assertEquals("someImage.png", user1.getAvatarPath());
        assertEquals(10, user1.getGamesPlayed());
        assertEquals(3, user1.getGamesWon());

        // compare data of user2
        assertEquals(1, user2.getUserId());
        assertEquals("Samy", user2.getDisplayName());
        assertEquals("someOtherImage.png", user2.getAvatarPath());
        assertEquals(6, user2.getGamesPlayed());
        assertEquals(6, user2.getGamesWon());
    }

    /**
     * Test if we can update one of the two users in the database
     */
    @Test
    public void updateProfileTest(){
        // insert two users
        insertTwoAccounts();

        // we'll change some info on user2
        UserInfo user2 = testDatabase.getProfile(1);
        user2.setDisplayName("Fredy");
        user2.setGamesPlayed(200);
        user2.setGamesWon(100);

        // apply changes to databasee
        try{
            testDatabase.updateProfile(user2);
        } catch(SQLException ex){
            ex.printStackTrace();
            assertTrue(false);
        }

        // get the new data and compare
        user2 = testDatabase.getProfile(1);
        assertEquals(1, user2.getUserId());
        assertEquals("Fredy", user2.getDisplayName());
        assertEquals("someOtherImage.png", user2.getAvatarPath());
        assertEquals(200, user2.getGamesPlayed());
        assertEquals(100, user2.getGamesWon());
    }

    /**
     * Test if we can insert a new chatroom into database.
     */
    @Test
    public void insertChatRoomTest() {
        // first create two chat rooms
        insertTwoChatRooms();

        try {
            // Get data from database
            Statement state = testConnection.createStatement();
            ResultSet rs = state.executeQuery("SELECT * FROM chat_room WHERE chat_name = 'Test'");

            //Loop over data and check if values match with our original values
            while (rs.next()) {
                assertEquals("Testroom", rs.getString("chat_name"));
            }

            rs = state.executeQuery("SELECT * FROM chat_room WHERE chat_name = 'Test2'");
            //Loop over data and check if values match with our original values
            while (rs.next()) {
                assertEquals("Testroom2", rs.getString("chat_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test if we get all chat rooms available in the database
     */
    @Test
    public void getAllChatRoomsTest() {
        // insert two chatrooms
        insertTwoChatRooms();

        ArrayList<String> chatRooms = testDatabase.getAllChatRooms();

        // we should only have 2 chatrooms in database at the moment
        assertEquals(2, chatRooms.size());

        // do names match entries?
        assertEquals("Testroom", chatRooms.get(0));
        assertEquals("Testroom2", chatRooms.get(1));
    }

    /**
     * Test to check if we can deelete a room from the database and
     * if the correlated chat log messages are deleted as well (CONSTRAINT ON DELETE CASCADE)
     */
    @Test
    public void deleteAChatRoomTest() {
        ArrayList<String> chatRooms;
        ArrayList<ChatMessage> chatMessages;

        // insert two users
        insertTwoAccounts();

        // insert two chatrooms
        insertTwoChatRooms();

        // insert two messages
        insertTwoMessages();

        // we should only have 2 chatrooms in database at the moment
        chatRooms = testDatabase.getAllChatRooms();
        assertEquals(2, chatRooms.size());
        // we should only have 1 chatmessage from "Testroom" in database at the moment
        chatMessages = testDatabase.getChatMessages("Testroom");
        assertEquals(1, chatMessages.size());

        // try to remove the first chat room. This should also remove any
        // chatlog entries referring to this chat room
        try {
            testDatabase.removeChatRoom("Testroom");
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }

        // first test if deleted from "chat_room" table
        chatRooms = testDatabase.getAllChatRooms();
        assertEquals(1, chatRooms.size());
        assertEquals("Testroom2", chatRooms.get(0));

        // then test if related messages from "chat_log" table are deleted as well
        chatMessages = testDatabase.getChatMessages("Testroom");
        assertEquals(0, chatMessages.size());
    }

    /**
     * Test if we can insert a chat message into database.
     */
    @Test
    public void insertChatMessagesTest() {
        // Insert two users
        insertTwoAccounts();

        // Insert two chat rooms
        insertTwoChatRooms();

        // Insert two chat messages
        insertTwoMessages();

        try {
            // Get data from database
            Statement state = testConnection.createStatement();

            // compare first message
            ResultSet rs = state.executeQuery("SELECT * FROM chat_log WHERE chat_name = 'Testroom'");

            //Loop over data and check if values match with our original values
            while (rs.next()) {
                assertEquals("Testroom", rs.getString("chat_name"));
                assertEquals(0, rs.getInt("user_id"));
                assertEquals("Wow, what a great game :)", rs.getString("chat_message"));
                assertNotEquals(0, rs.getLong("timestamp"));
            }

            // compare second message
            rs = state.executeQuery("SELECT * FROM chat_log WHERE chat_name = 'Testroom2'");

            //Loop over data and check if values match with our original values
            while (rs.next()) {
                assertEquals("Testroom2", rs.getString("chat_name"));
                assertEquals(1, rs.getInt("user_id"));
                assertEquals("Ye, this game deserves an 'A'!", rs.getString("chat_message"));
                assertNotEquals(0, rs.getLong("timestamp"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void getChatMessagesTest() {
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();

        // Insert two users
        insertTwoAccounts();

        // Insert two chat rooms
        insertTwoChatRooms();

        // Insert two chat messages
        insertTwoMessages();

        // get the message from the first chatroom
        chatMessages = testDatabase.getChatMessages("Testroom");
        // We only added 1 message to database in this chatroom
        assertTrue(chatMessages.size() == 1);

        // check if first message match our insert
        assertEquals("Testroom", chatMessages.get(0).getChatName());
        assertEquals(0, chatMessages.get(0).getUserId());
        assertEquals("Wow, what a great game :)", chatMessages.get(0).getChatMessage());


        // get the message from the second chatroom
        chatMessages = testDatabase.getChatMessages("Testroom2");
        // We only added 1 message to database in this chatroom
        assertTrue(chatMessages.size() == 1);

        // check if second message match our insert
        assertEquals("Testroom2", chatMessages.get(0).getChatName());
        assertEquals(1, chatMessages.get(0).getUserId());
        assertEquals("Ye, this game deserves an 'A'!", chatMessages.get(0).getChatMessage());
    }
}