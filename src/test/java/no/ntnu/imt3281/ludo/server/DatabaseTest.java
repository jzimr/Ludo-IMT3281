package no.ntnu.imt3281.ludo.server;

import org.junit.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DatabaseTest {
    private static final String dbURL = "jdbc:derby:./ludoTestDB";
    private static Database testDatabase;       // our test database
    private static Connection testConnection;          // database connection to execute sql statements

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
            // fail testing if not
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
            assertEquals("USER_NAME", resultSet.getMetaData().getColumnName(2));
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

        // check if tables have all the required columns and only has 5 columns
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

        // check if tables have all the required columns and only has 5 columns
        try {
            assertEquals("CHAT_NAME", resultSet.getMetaData().getColumnName(1));
            assertEquals(1, resultSet.getMetaData().getColumnCount());
        } catch (SQLException ex) {
            assertFalse(true);
        }
    }

    /**
     * Helper function to insert two basic users into the database
     */
    private void insertTwoUsers() {
        try {
            testDatabase.insertUser("Boby", "someImage.png", 10, 3);
            testDatabase.insertUser("Samy", "someOtherImage.png", 6, 6);
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
     * Testing if we successfully add and can retrieve 2 users in the database
     * It also tests if the database auto increments the unique ID of the users
     */
    @Test
    public void insertUserTest() {
        // insert two users
        insertTwoUsers();

        try {
            Statement state = testConnection.createStatement();

            // execute SELECT query
            ResultSet rs = state.executeQuery("SELECT * FROM user_info WHERE user_id=0");

            // loop over data of user 1
            while (rs.next()) {
                assertEquals(0, rs.getInt("user_id"));
                assertEquals("Boby", rs.getString("user_name"));
                assertEquals("someImage.png", rs.getString("avatar_path"));
                assertEquals(10, rs.getInt("games_played"));
                assertEquals(3, rs.getInt("games_won"));
            }

            // execute SELECT query
            rs = state.executeQuery("SELECT * FROM user_info WHERE user_id=1");

            // loop over data of user 2
            while (rs.next()) {
                assertEquals(1, rs.getInt("user_id"));
                assertEquals("Samy", rs.getString("user_name"));
                assertEquals("someOtherImage.png", rs.getString("avatar_path"));
                assertEquals(6, rs.getInt("games_played"));
                assertEquals(6, rs.getInt("games_won"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void getUserTest() {
        // insert two users
        insertTwoUsers();

        // get both users
        UserInfo user1 = testDatabase.getUser(0);
        UserInfo user2 = testDatabase.getUser(1);

        // compare data of user1
        assertEquals(0, user1.getUserId());
        assertEquals("Boby", user1.getUserName());
        assertEquals("someImage.png", user1.getAvatarPath());
        assertEquals(10, user1.getGamesPlayed());
        assertEquals(3, user1.getGamesWon());

        // compare data of user2
        assertEquals(1, user2.getUserId());
        assertEquals("Samy", user2.getUserName());
        assertEquals("someOtherImage.png", user2.getAvatarPath());
        assertEquals(6, user2.getGamesPlayed());
        assertEquals(6, user2.getGamesWon());
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Test if we can insert a chat message into database.
     */
    @Test
    public void insertChatMessagesTest() {
        // Insert two users
        insertTwoUsers();

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
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void getChatMessagesTest() {
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();

        // Insert two users
        insertTwoUsers();

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