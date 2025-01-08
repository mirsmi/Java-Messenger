package Controller;

import Model.Commands;
import Model.Message;
import Model.User;
import View.ChatView;
import View.MainPage;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the client-side application
 * It handles communication with the server and manages user sessions
 */
public class Client extends Thread {

    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static final int serverPort = 2323;
    private static final String serverAddress = "127.0.0.1";
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private User currentUser;
    private final MainPage mainPage;
    private final ConcurrentHashMap<LinkedList<String>, User> chatViewsMap = new ConcurrentHashMap<>();
    private final ViewController viewController;

    /**
     * Constructor used for logging in an existing user
     * @param mainPage Reference to the main client application
     * @param username The username of the user
     * @param password The password of the user
     */
    public Client(ViewController viewController, MainPage mainPage, String username, String password) {
        this.mainPage = mainPage;
        this.viewController = viewController;
        initializeSocket();
        User user = new User(username, Encryptor.encryptPassword(password));
        sendToStream(new Object[]{Commands.LOGIN_REQUEST, user});
        start();
    }

    /**
     * Constructor used for registering a new user
     * @param mainPage Reference to the main client application
     * @param username The username of the new user
     * @param password The password of the new user
     * @param firstName The first name of the new user
     * @param lastName The last name of the new user
     * @param imagePath Path to the user's profile image
     */
    public Client(MainPage mainPage, ViewController viewController, String username, String password, String firstName, String lastName, String imagePath) {
        this.mainPage = mainPage;
        this.viewController = viewController;
        initializeSocket();
        User user = new User(username, Encryptor.encryptPassword(password), firstName, lastName, imagePath);
        sendToStream(new Object[]{Commands.REGISTRATION_REQUEST, user});
        start();
    }

    /**
     * Initializes the socket and input/output streams for communication with the server
     */
    private void initializeSocket() {
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not establish a connection with the server!", e);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                try {
                    Object receivedObject = inputStream.readObject();
                    //process commands received from the server (e.g. login)
                    if (receivedObject instanceof Object[] object) {
                        handleServerCommand(object);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    logger.log(Level.WARNING, "Something went wrong reading object from server!", e.toString());
                    break;
                }
            }
        } finally {
            closeAllConnections();
        }
    }

    /**
     * Processes commands received from the server, such as login status or new messages
     * @param object The command object from the server, typically an array where the first element is the command type
     */
    private void handleServerCommand(Object[] object) {
        Commands command = (Commands) object[0];
        switch (command) {
            case LOGIN_SUCCESSFUL -> handleSuccessfulLogin(object);
            case LOGIN_UNSUCCESSFUL -> handleUnsuccessfulLogin();
            case REGISTRATION_SUCCESSFUL -> handleSuccessfulRegistration(object);
            case REGISTRATION_UNSUCCESSFUL -> handleUnsuccessfulRegistration();
            case CONNECTED_USER -> addToActiveUsers((String[]) object[1]);
            case START_CHATTING -> handleChat(object);
            case SHOW_MESSAGE -> showMessage(object);
            case SHOW_CONTACTS -> updateContacts((LinkedList<String>) object[2]);
            case REMOVE_ACTIVE_USER -> removeFromActiveUsers((String) object[1]);
            case SHOW_STORED_MESSAGES -> showStoredMessages(object);
            case CLOSE_CHAT_VIEW -> closeChatView(object);
        }
    }

    /**
     * Handles a successful login response from the server
     * @param object The response object containing user data
     */
    private void handleSuccessfulLogin(Object[] object) {
        currentUser = (User) object[1];
        currentUser.setOnline(true);
        viewController.showFrontPage(currentUser, false);
        Thread.currentThread().setName(currentUser.getUsername() + " Client thread");
    }

    /**
     * Handles a successful registration response from the server
     * @param object The response object containing user data
     */
    private void handleSuccessfulRegistration(Object[] object) {
        currentUser = (User) object[1];
        currentUser.setOnline(true);
        viewController.showFrontPage(currentUser, true);
        Thread.currentThread().setName(currentUser.getUsername() + " Client thread");
    }

    /**
     * Handles an unsuccessful login attempt
     */
    private void handleUnsuccessfulLogin() {
        //inform the user of a failed login attempt (possible causes: incorrect username/password, server issue)
        JOptionPane.showMessageDialog(null, "Login failed. Please check your username or password!", "Error", JOptionPane.ERROR_MESSAGE);
        closeAllConnections();
    }

    /**
     * Handles an unsuccessful registration attempt
     */
    private void handleUnsuccessfulRegistration() {
        //notify the user of registration failure (e.g., username already taken, server error)
        JOptionPane.showMessageDialog(null, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
        closeAllConnections();
    }

    /**
     * Manages starting a chat session with the given users
     * @param object The command object containing user details
     */
    private void handleChat(Object[] object) {
        LinkedList<String> userLinkedList = (LinkedList<String>) object[1];

        //avoid opening duplicate chat views for the same users
        if (!mainPage.getUsersChatViews().containsKey(userLinkedList) && currentUser.isOnline()) {
            ChatView chatView = new ChatView(currentUser.getUsername(), userLinkedList, viewController);
            mainPage.getUsersChatViews().put(userLinkedList, chatView);
        }
        chatViewsMap.put(userLinkedList, currentUser);
    }

    /**
     * Displays a received message in the appropriate chat view
     * @param object The command object containing the message
     */
    private void showMessage(Object[] object) {
        Message messageToShow = (Message) object[1];
        ChatView chatView = mainPage.getUsersChatViews().get(messageToShow.getRecipientList());

        if (chatView != null) {
            updateChatView(messageToShow, chatView);
        }
    }

    /**
     * Displays stored messages for the current user
     * @param object The command object containing stored messages
     */
    private void showStoredMessages(Object[] object) {
        ChatView chatView = new ChatView(viewController);
        chatView.showChatView(currentUser.getUsername());
        for (Message message : (ArrayList<Message>) object[1]) {
            updateChatView(message, chatView);
        }
    }

    /**
     * Closes the chat view for a given list of users
     * @param object The command object containing the user list
     */
    private void closeChatView(Object[] object) {
        LinkedList<String> chatList = (LinkedList<String>) object[1];
        chatViewsMap.remove(chatList);
        ChatView chatView = mainPage.getUsersChatViews().get(chatList);
        if(chatView != null){
            chatView.getChatViewFrame().dispose();
        }

        mainPage.getUsersChatViews().remove(chatList);
    }

    /**
     * Adds selected users to the contact list
     * @param selectedUsers The users to add to contacts
     */
    public void addToContacts(Object[] selectedUsers) {
        LinkedList<String> users = new LinkedList<>();
        users.add(currentUser.getUsername());
        users.add((String) selectedUsers[0]);
        sendToStream(new Object[]{Commands.ADD_TO_CONTACTS_REQUEST, users});
    }

    /**
     * Adds connected users to the active users list
     * @param connectedUsers The users to add to the active list
     */
    public void addToActiveUsers(String[] connectedUsers) {
        ArrayList<String> activeUserList = mainPage.getActiveUsers();
        synchronized (activeUserList) {
            for (String connectedUser : connectedUsers) {
                if (!connectedUser.equals(currentUser.getUsername()) && !activeUserList.contains(connectedUser)) {
                    activeUserList.add(connectedUser);
                }
            }
            mainPage.getActiveUsersList().setListData(activeUserList.toArray());
        }
    }

    /**
     * Updates the contact list with new contacts
     * @param newContacts The new contacts to add
     */
    public void updateContacts(LinkedList<String> newContacts) {
        ArrayList<Object> contacts = mainPage.getContacts();
        synchronized (contacts) {
            for (String contact : newContacts) {
                if (!contacts.contains(contact) && !contact.equals(currentUser.getUsername())) {
                    contacts.add(contact);
                }
            }
            mainPage.getContactList().setListData(contacts.toArray());
        }
    }

    /**
     * Updates the chat view with a new message
     * @param message The message to display
     * @param chatView The chat view to update
     */
    public void updateChatView(Message message, ChatView chatView) {
        chatView.getMessagesList().add(message);
        chatView.getChatField().setListData(chatView.getMessagesList().toArray());
        chatView.getMessageTextField().setText("");
    }

    /**
     * Initiates a chat session with the selected users
     * @param selectedUsers The users to start chatting with
     */
    public void initiateChat(Object[] selectedUsers) {
        LinkedList<String> usernames = new LinkedList<>();
        usernames.add(currentUser.getUsername());
        for (Object user : selectedUsers){
            usernames.add(user.toString());
        }

        ChatView chatView = new ChatView(currentUser.getUsername(), usernames, viewController);
        mainPage.getUsersChatViews().put(usernames, chatView);
        sendToStream(new Object[]{Commands.CHAT_REQUEST, usernames});
    }

    /**
     * Removes a user from the active users list
     * @param username The username to remove
     */
    public void removeFromActiveUsers(String username) {
        if (username.equals(currentUser.getUsername())) {
            return;
        }
        List<String> activeUserList = mainPage.getActiveUsers();
        if (activeUserList.remove(username)) {
            mainPage.getActiveUsersList().setListData(activeUserList.toArray(new String[0]));
        }
    }

    /**
     * Sends a message to the server, which includes text, image, and other details
     * @param textMessage The text content of the message
     * @param imageIcon An optional image attached to the message
     * @param recipients The list of users to send the message to
     * @param filePath The file path of any attached image or file
     */
    public void sendMessage(String textMessage, ImageIcon imageIcon, LinkedList<String> recipients, String filePath) {
        Message message = new Message.Builder()
                .withText(textMessage)
                .withImage(imageIcon)
                .withSentBy(currentUser.getUsername())
                .addRecipients(recipients)
                .withSendTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .withImagePath(filePath)
                .build();
        sendToStream(new Object[]{Commands.SEND_MESSAGE_REQUEST, message});
    }

    /**
     * Gets the current user of this client
     * @return The current user object
     */
    public User getUser() {
        return currentUser;
    }

    /**
     * Sends an object to the server via the output stream
     * @param object The object to send
     */
    private void sendToStream(Object object) {
        try {
            synchronized (outputStream) {
                outputStream.writeObject(object);
                outputStream.flush();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not send the object to the server", e);
        }
    }

    /**
     * Closes all connections and streams associated with the client
     */
    public void closeAllConnections() {
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
            if (mainPage != null) mainPage.logout();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing the client.", e);
        }
    }

    /**
     * Logs out the current user and closes all connections
     */
    public void logout() {
        sendToStream(new Object[]{Commands.LOGOUT_REQUEST, currentUser.getUsername()});
        closeAllConnections();
    }

    /**
     * Sends a request to the server to close the chat view for the specified list of users
     * @param people The list of users whose chat views should be closed
     */
    public void closeChatViewForUsers(LinkedList<String> people) {
        sendToStream(new Object[]{Commands.CLOSE_CHAT_VIEW_REQUEST, people});
    }
}
