package Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseManager {

    //singleton instance
    private static volatile DataBaseManager instance;

    //database connection constants
    private static final String DATABASE_URL = "DATABASE_LINK";
    private static final String USERNAME = "DATABASE_USERNAME";
    private static final String PASSWORD = "DATABASE_PASSWORD";

    //SQL query strings
    private static final String CREATE_USERS_SQL = "INSERT INTO users (username, firstname, lastname, password, profile_image) VALUES ( ?, ?, ?, ?, ?)";
    private static final String CREATE_CONTACT = "INSERT INTO saved_contacts (username, contact) VALUES ( ?, ?)";
    private static final String CREATE_CHAT = "INSERT INTO saved_chats (timestamp, sender, receiver, message, image) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?)";
    private static final String DELETE_SAVED_CHATS = "DELETE FROM saved_chats WHERE receiver = ?";
    private static final String GET_USER_INFO = "SELECT * FROM users WHERE username = ?";
    private static final String GET_CONTACTS = "SELECT * FROM saved_contacts WHERE username = ?";
    private static final String GET_SAVED_CHAT = "SELECT * FROM saved_chats WHERE receiver = ?";
    private static final String VERIFY_USER_SQL = "SELECT * FROM users WHERE username = ? AND password = ?";
    private static final String REGISTER_CONTACT_SQL = "SELECT * FROM saved_contacts WHERE user = ?;";
    private static final String REGISTER_USER_SQL = "SELECT * FROM users WHERE username = ?;";

    /**
     * Private constructor to prevent instantiation
     */
    private DataBaseManager() {

    }

    /**
     * Method to get the singleton
     * @return the singleton
     */
    public static DataBaseManager getInstance() {
        if (instance == null) {
            synchronized (DataBaseManager.class) {
                if (instance == null) {
                    instance = new DataBaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Establish and return a database connection
     * @return Connection instance
     */
    public Connection getDatabaseConnection() {
        try {
            return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Could not establish a database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a new contact in the database
     * @param username the user
     * @param contact contact the user decided to save
     * @return true if everything went okay, if not, return false
     */
    public synchronized boolean registerContact(String username, String contact) {
        try (Connection con = getDatabaseConnection()) {
            PreparedStatement usernameStmt = con.prepareStatement(REGISTER_CONTACT_SQL);
            usernameStmt.setString(1, username);
            ResultSet usernameRs = usernameStmt.executeQuery();

            if (!usernameRs.next()) { //contact does not exist for user
                PreparedStatement preparedStatement = con.prepareStatement(CREATE_CONTACT);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, contact);
                preparedStatement.executeUpdate();
                System.out.println("Registered contact for user: " + username);
                return true;
            } else {
                System.out.println("Error! This contact already exists for the user");
            }
        } catch (RuntimeException | SQLException e) {
            System.out.println("Contact could not be registered in the database!");
        }
        return false;
    }

    /**
     * Register new user in the database
     * @param username unique username
     * @param password password
     * @param firstName firstname
     * @param lastName lastname
     * @param imagePath profile image path
     * @return true if everything went okay, if not, return false
     */
    public synchronized boolean registerUser(String username, String password, String firstName, String lastName, String imagePath) {
        try (Connection con = getDatabaseConnection()) {
            PreparedStatement usernameStmt = con.prepareStatement(REGISTER_USER_SQL);
            usernameStmt.setString(1, username);
            ResultSet usernameRs = usernameStmt.executeQuery();

            if (!usernameRs.next()) { //username does not exist
                PreparedStatement preparedStatement = con.prepareStatement(CREATE_USERS_SQL);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, firstName);
                preparedStatement.setString(3, lastName);
                preparedStatement.setString(4, password);

                //store the profile image
                if (imagePath != null) {
                    File imageFile = new File(imagePath);
                    InputStream fis = new FileInputStream(imageFile);
                    preparedStatement.setBinaryStream(5, fis, (int) imageFile.length());
                } else {
                    preparedStatement.setNull(5, Types.BLOB);
                    System.out.println("No image provided");
                }

                preparedStatement.executeUpdate();
                System.out.println("User registered with username: " + username);
                return true;
            } else {
                System.out.println("Error! This username already exists");
            }
        } catch (RuntimeException | SQLException | IOException e) {
            System.out.println("Could not register the user!");
        }
        return false;
    }

    /**
     * Method that fetches all info from the database for a certain user
     * @param username user
     * @return a list of maps
     */
    public List<Map<String, Object>> getAdditionalUserInfo(String username) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (Connection con = getDatabaseConnection();
             PreparedStatement preparedStatement = con.prepareStatement(GET_USER_INFO)) {
            preparedStatement.setString(1, username);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("username", rs.getString("username"));
                    row.put("firstname", rs.getString("firstname"));
                    row.put("lastname", rs.getString("lastname"));
                    row.put("password", rs.getString("password"));
                    row.put("image", rs.getBytes("profile_image"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resultList;
    }

    /**
     * Fetches all saved chats for a user
     * @param username the user
     * @return a list of maps contains saved chats
     */
    public List<Map<String, Object>> fetchSavedChats(String username) {
        List<Map<String, Object>> chats = new ArrayList<>();
        try (Connection con = getDatabaseConnection();
             PreparedStatement preparedStatement = con.prepareStatement(GET_SAVED_CHAT)) {
            preparedStatement.setString(1, username);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> chat = new HashMap<>();
                    chat.put("timestamp", rs.getTimestamp("timestamp"));
                    chat.put("sender", rs.getString("sender"));
                    chat.put("receiver", rs.getString("receiver"));
                    chat.put("message", rs.getString("message"));
                    chat.put("image", rs.getBytes("image"));
                    chats.add(chat);
                }
            }
        } catch (SQLException e) {
            System.out.println("No saved chats!");
        }
        return chats;
    }

    /**
     * Removes all saved chats for a certain user
     * @param user the user
     */
    public void removeSavedChats(String user) {
        try (Connection connection = getDatabaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SAVED_CHATS)) {
            preparedStatement.setString(1, user);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("No chats!");
        }
    }

    /**
     * Fetches all contacts for a certain user
     * @param username the user
     * @return a list of maps
     */
    public List<Map<String, Object>> getContacts(String username) {
        List<Map<String, Object>> contacts = new ArrayList<>();
        try (Connection con = getDatabaseConnection();
             PreparedStatement preparedStatement = con.prepareStatement(GET_CONTACTS)) {
            preparedStatement.setString(1, username);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> contact = new HashMap<>();
                    contact.put("username", rs.getString("username"));
                    contact.put("contact", rs.getString("contact"));
                    contacts.add(contact);
                }
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong fetching info from database!");
        }
        return contacts;
    }


    /**
     * Checks if the user exists in the database
     * @param username the username
     * @param password the password
     * @return true if the user exists, otherwise return false
     */
    public boolean verifyUser(String username, String password) {
        try (Connection connection = getDatabaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(VERIFY_USER_SQL)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves a message in the database for users that are online
     * @param sender the sender of the message
     * @param receiver the receiver
     * @param message the message
     * @param imagePath the image
     */
    public void saveChat(String sender, String receiver, String message, String imagePath) {
        try (Connection con = getDatabaseConnection();
             PreparedStatement preparedStatement = con.prepareStatement(CREATE_CHAT)) {
            preparedStatement.setString(1, sender);
            preparedStatement.setString(2, receiver);
            preparedStatement.setString(3, message);

            if (imagePath != null) {
                File imageFile = new File(imagePath);
                InputStream fis = new FileInputStream(imageFile);
                preparedStatement.setBinaryStream(4, fis, (int) imageFile.length());
            } else {
                preparedStatement.setBinaryStream(4, null);
                System.out.println("No image provided!");
            }

            preparedStatement.executeUpdate();
            System.out.println("Chat saved successfully");
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
