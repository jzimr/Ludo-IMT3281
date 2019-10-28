package no.ntnu.imt3281.ludo.server;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.*;

public class DatabaseTest {
    private static final String dbURL = "jdbc:derby:./ludoTestDB";
    private static Database testDatabase;       // our test database
    private static Connection testConnection;          // database connection to execute sql statements

    /**
     * Sets up database before any other test is run and
     * checks if we can connect to it
     */
    @BeforeClass
    public static void setupDatabase(){
        // create test database instance
        try{
            testDatabase = Database.constructTestDatabase(dbURL);
        } catch(Exception ex){
            assertFalse(true);
        }

        // we are supposed to get the connection
        try{
            testConnection = DriverManager.getConnection(dbURL);
        } catch(SQLException e){
            System.out.println(e.getMessage());
            // fail testing if not
            assertFalse(true);
        }
    }

    /**
     * Remove all data at end of tests (excluding DROP table)
     */
    @AfterClass
    public static void purgeDatabase(){
        // remove all data from records
        try{
            Statement statement = testConnection.createStatement();
            statement.execute("DROP TABLE user_info");
            statement.execute("DROP TABLE chat_log");
            statement.execute("DROP TABLE chat_room");
        } catch(SQLException ex){
            System.out.println(ex.getMessage());
            assertFalse(true);
        }
    }


    /**
     * Tests that our database was correctly setup with the desired tables and columns
     */
    @Test
    public void initialTest(){
        Statement statement;
        ResultSet resultSet = null;

        // test user_info table
        try{
            statement = testConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM user_info");
        } catch(SQLException ex){
            assertFalse(true);
        }

        // check if tables have all the required columns and only has 5 columns
        try{
            assertEquals("USER_ID", resultSet.getMetaData().getColumnName(1));
            assertEquals("USER_NAME", resultSet.getMetaData().getColumnName(2));
            assertEquals("AVATAR_PATH", resultSet.getMetaData().getColumnName(3));
            assertEquals("GAMES_PLAYED", resultSet.getMetaData().getColumnName(4));
            assertEquals("GAMES_WON", resultSet.getMetaData().getColumnName(5));
            assertEquals(5, resultSet.getMetaData().getColumnCount());
        } catch(SQLException ex){
            assertFalse(true);
        }

        // test chat_log table
        try{
            statement = testConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM chat_log");
        } catch(SQLException ex){
            assertFalse(true);
        }

        // check if tables have all the required columns and only has 5 columns
        try{
            assertEquals("CHAT_NAME", resultSet.getMetaData().getColumnName(1));
            assertEquals("USER_ID", resultSet.getMetaData().getColumnName(2));
            assertEquals("CHAT_MESSAGE", resultSet.getMetaData().getColumnName(3));
            assertEquals("TIMESTAMP", resultSet.getMetaData().getColumnName(4));
            assertEquals(4, resultSet.getMetaData().getColumnCount());
        } catch(SQLException ex){
            assertFalse(true);
        }

        // test chat_room table
        try{
            statement = testConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM chat_log");
        } catch(SQLException ex){
            assertFalse(true);
        }

        // check if tables have all the required columns and only has 5 columns
        try{
            assertEquals("CHAT_NAME", resultSet.getMetaData().getColumnName(1));
            assertEquals("USER_ID", resultSet.getMetaData().getColumnName(2));
            assertEquals("CHAT_MESSAGE", resultSet.getMetaData().getColumnName(3));
            assertEquals("TIMESTAMP", resultSet.getMetaData().getColumnName(4));
            assertEquals(4, resultSet.getMetaData().getColumnCount());
        } catch(SQLException ex){
            assertFalse(true);
        }
    }

    /**
     * Testing if we successfully add and can retrieve 2 users in the database
     * It also tests if the database auto increments the unique ID of the users
     */
    @Test
    public void insertUserTest(){
        //Try to insert user into db and check if the data is correct.
        try {
            Statement state = testConnection.createStatement();

            // Insert user 1 into database
            testDatabase.insertUser("Boby", "someImage.png", 10, 3);
            // execute SELECT query
            ResultSet rs = state.executeQuery("SELECT * FROM user_info WHERE user_id=0");

            // loop over data of user 1
            while(rs.next()) {
                assertEquals(0, rs.getInt("user_id"));
                assertEquals("Boby", rs.getString("user_name"));
                assertEquals("someImage.png", rs.getString("avatar_path"));
                assertEquals(10, rs.getInt("games_played"));
                assertEquals(3, rs.getInt("games_won"));
            }

            // Insert user 2 into database
            testDatabase.insertUser("Samy", "someOtherImage.png", 6, 6);
            // execute SELECT query
            rs = state.executeQuery("SELECT * FROM user_info WHERE user_id=1");

            // loop over data of user 2
            while(rs.next()) {
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

    /**
     * Test if we can insert a new chatroom into database.
     */
    @Test
    public void insertChatRoomTest(){
        //Try to insert chat room into db and check if the data is correct.
        try {
            testDatabase.insertChatRoom("Test");
        } catch (SQLException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }

        try {
            // Get data from database
            Statement state = testConnection.createStatement();
            ResultSet rs = state.executeQuery("SELECT * FROM chat_room");

            //Loop over data and check if values match with our original values
            while(rs.next()) {
                assertEquals("Test", rs.getString("room_name"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Test if we can insert a chat message into database.
     */
    @Test
    public void insertChatMessageTest(){

        //Try to retrieve data from db and check if the data is correct.
        try {
            testDatabase.insertChatMessage("Test",1, "Hello Test");
        } catch (SQLException e) {
            assertTrue( "Failed to insert chat message into chat_log table",false);
            System.out.println(e.getMessage());
        }

        try {
            // Get data from database
            Statement state = testConnection.createStatement();
            ResultSet rs = state.executeQuery("SELECT * FROM chat_log");

            //Loop over data and check if values match with our original values
            while(rs.next()) {
                assertEquals("Test", rs.getString("chat_name"));
                assertEquals(String.valueOf(1), rs.getString("user_id"));
                assertEquals("Hello Test", rs.getString("chat_message"));
                assertNotEquals(String.valueOf(0), rs.getString("timestamp"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}