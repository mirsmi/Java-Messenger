package Controller;

import Model.Commands;
import Model.Message;
import Model.User;
import View.MainPage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Server class to handle incoming client connections and requests
 */
public class Server extends Thread {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final int serverPort = 2323;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool(); //thread Pool to handle clients
    private final DataBaseManager dBManager = DataBaseManager.getInstance();
    public static final CopyOnWriteArrayList<LinkedList<String>> userChats = new CopyOnWriteArrayList<>();
    public static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, MainPage> usersApps = new ConcurrentHashMap<>();

    /**
     * Initializes the server socket and starts the server thread
     */
    public Server() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start the server!", e);
            throw new RuntimeException("Failed to start the server!", e);
        }
        start();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Server thread");

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                threadPool.execute(new ClientHandler(socket)); //thread pool to manage threads
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "There are some problems with the server!", e);
        } finally {
            try {
                threadPool.shutdown(); //shut down thread pool
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Problems closing the server!", e);
            }
        }
    }

    /**
     * Handles individual client connections and requests
     */
    public class ClientHandler implements Runnable {
        private final Socket socket;
        private final ObjectOutputStream oos;
        private final ObjectInputStream ois;

        /**
         * Initializes input and output streams for the client socket
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to initialize client streams!", e);
                throw new RuntimeException("Failed to initialize client streams", e);
            }
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    Object objectSentToStream = ois.readObject();

                    if (objectSentToStream instanceof Object[] object) {
                        Commands request = (Commands) object[0];

                        switch (request) {
                            case SEND_MESSAGE_REQUEST -> sendMessage(object);
                            case LOGIN_REQUEST -> loginUser(object);
                            case REGISTRATION_REQUEST -> registerUser(object);
                            case CHAT_REQUEST -> createChat(object);
                            case ADD_TO_CONTACTS_REQUEST -> addToContacts(object);
                            case LOGOUT_REQUEST -> logoutUser(object);
                            case CLOSE_CHAT_VIEW_REQUEST -> closeChat(object);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.log(Level.SEVERE, "Client disconnected or error occurred!", e);
            } finally {
                closeAll(Thread.currentThread().getName());
            }
        }

        /**
         * Sends a message to the appropriate recipients
         * If the recipient is online, the message is sent directly
         * Otherwise, the message is saved in the database for later delivery
         * @param msg an array containing the message object at index 1
         */
        private void sendMessage(Object[] msg) {
            Message message = (Message) msg[1];

            LinkedList<String> list = new LinkedList<>(message.getRecipientList()); //list of recipients
            LinkedList<String> offlineUsers = new LinkedList<>(); //list to track offline users

            for (String u : list) {
                ClientHandler clientHandler = clients.get(u); //check if recipient is online
                if (clientHandler != null) {
                    clientHandler.writeToStream(new Object[]{Commands.SHOW_MESSAGE, message}); //deliver message
                } else {
                    offlineUsers.add(u); //mark user as offline
                }
            }

            for (String u : offlineUsers) {
                //save the message in the database for offline users
                dBManager.saveChat(message.getSentBy(), u, message.getText(), message.getImagePath());
            }
        }

        /**
         * Handles user login
         * Verifies credentials, retrieves user information, and initializes the client session
         * @param user an array where the user object is at index 1
         */
        private void loginUser(Object[] user) {
            User theUser = (User) user[1];
            String username = theUser.getUsername();
            String password = theUser.getPassword();
            put(username, this); //register the client handler

            if (dBManager.verifyUser(username, password)) { //check if credentials are correct
                List<Map<String, Object>> map = dBManager.getAdditionalUserInfo(username); //fetch user details
                userInfoFromDatabase(map); //send user details to the client
                List<Map<String, Object>> map2 = dBManager.getContacts(username); //fetch user contacts
                loadContacts(map2, username); //send contacts to the client
                loadSavedChats(dBManager.fetchSavedChats(username), username); //load any saved (offline) chats
                Thread.currentThread().setName(username); //set thread name for debugging
                updateActiveUsers(); //notify all clients about active users
            } else {
                //if credentials are incorrect, notify the client and close the session
                writeToStream(new Object[]{Commands.LOGIN_UNSUCCESSFUL, "Wrong credentials!"});
                closeAll(username);
            }
        }

        /**
         * Registers a new user in the database
         * Sends a success or failure response to the client
         * @param user an array where the user object is at index 1
         */
        private void registerUser(Object[] user) {
            User theUser = (User) user[1];
            String username = theUser.getUsername();
            put(username, this); //register the client handler

            //attempt to register the user in the database
            boolean canRegister = dBManager.registerUser(
                    username,
                    theUser.getPassword(),
                    theUser.getFirstName(),
                    theUser.getLastName(),
                    theUser.getImagePath()
            );

            Object[] messageToClient = canRegister
                    ? new Object[]{Commands.REGISTRATION_SUCCESSFUL, theUser} //success response
                    : new Object[]{Commands.REGISTRATION_UNSUCCESSFUL, theUser}; //failure response

            writeToStream(messageToClient); //notify the client about the result
            if (canRegister) {
                updateActiveUsers(); //notify all clients about the new user
            } else {
                closeAll(username); //close the session for unsuccessful registration
            }

            Thread.currentThread().setName(username); //set thread name for debugging
        }

        /**
         * Adds a new contact to the user's contact list in the database
         * Updates the client's contact list upon successful addition
         * @param contact an array where the contact details are at index 1
         */
        private void addToContacts(Object[] contact) {
            LinkedList<String> userList = (LinkedList<String>) contact[1]; //user and contact details
            if (dBManager.registerContact(userList.get(0), userList.get(1))) {
                //reload and send updated contact list to the client
                loadContacts(dBManager.getContacts(userList.get(0)), userList.get(0));
            }
        }

        /**
         * Logs out a user by closing their session and notifying other clients
         * @param user an array where the username is at index 1
         */
        private void logoutUser(Object[] user) {
            String username = (String) user[1];
            closeAll(username); //close the user's session
        }

        /**
         * Creates a new chat session with the selected users
         * Ensures thread-safe registration of the chat
         * @param users an array where the list of users in the chat is at index 1
         */
        private void createChat(Object[] users) {
            LinkedList<String> userList = (LinkedList<String>) users[1]; //list of users in the chat
            synchronized (userChats) { //thread-safe access to chat list
                if (!userChats.contains(userList)) {
                    registerChat(userList); //register the chat session
                    startChatting(userList); //notify clients to start chatting
                }
            }
        }

        /**
         * Closes a chat session
         * Sends a command to all users in the chat to close their chat view
         * and removes the chat from the system
         * @param userList an array where the list of users in the chat is at index 1
         */
        private void closeChat(Object[] userList) {
            LinkedList<String> users = (LinkedList<String>) userList[1];
            for (String username : users) {
                ClientHandler clientHandler = clients.get(username); //get the client handler for each user
                if (clientHandler != null) {
                    clientHandler.writeToStream(new Object[]{Commands.CLOSE_CHAT_VIEW, users}); //notify clients to close the chat view
                }
            }
            deleteChat(users); //remove the chat from the system
        }

        /**
         * Fetches and loads saved chats for a user upon login
         * Retrieves messages from the database, converts them to Message objects, and sends them to the client
         * @param chatList a list of saved chat data fetched from the database
         * @param receiver the username of the user receiving the chats
         */
        private void loadSavedChats(List<Map<String, Object>> chatList, String receiver) {
            ArrayList<Message> messages = new ArrayList<>();
            try {
                for (Map<String, Object> chat : chatList) {
                    String timestamp = String.valueOf(chat.get("timestamp")); //fetch timestamp
                    String sender = (String) chat.get("sender"); //fetch sender's username
                    String text = (String) chat.get("message"); //fetch message text
                    byte[] image = (byte[]) chat.get("image"); //fetch image (if any)

                    //build the Message object using the Message.Builder
                    Message.Builder builder = new Message.Builder()
                            .withText(text)
                            .withSentBy(sender)
                            .withSendTime(timestamp)
                            .addRecipients(Collections.singletonList(receiver));

                    if (image != null) {
                        builder.withImage(convertToImageIcon(convertToBufferedImage(image))); //add image if available
                    }

                    messages.add(builder.build()); //add the built message to the list
                }

                if (!messages.isEmpty()) {
                    writeToStream(new Object[]{Commands.SHOW_STORED_MESSAGES, messages}); //send stored messages to the client
                }

                dBManager.removeSavedChats(receiver); //remove delivered chats from the database
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error loading saved chats for " + receiver, e);
            }
        }

        /**
         * Converts a byte array representing an image to a BufferedImage object
         * @param imageBytes the byte array containing the image data
         * @return a BufferedImage object
         * @throws IOException if an I/O error occurs during conversion
         */
        private static BufferedImage convertToBufferedImage(byte[] imageBytes) throws IOException {
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        }

        /**
         * Converts a BufferedImage to an ImageIcon, scaling it to a fixed size
         * @param img the BufferedImage to convert
         * @return an ImageIcon object
         */
        private static ImageIcon convertToImageIcon(BufferedImage img) {
            Image scaledImage = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH); //scale the image
            return new ImageIcon(scaledImage);
        }

        /**
         * Loads a user's contacts upon login
         * Retrieves contact data from the database and sends it to the client
         * @param contacts a list of saved contacts
         * @param username the username of the client receiving the contacts
         */
        private void loadContacts(List<Map<String, Object>> contacts, String username) {
            LinkedList<String> contactsList = new LinkedList<>();
            for (Map<String, Object> contact : contacts) {
                contactsList.add((String) contact.get("contact")); //add each contact's username to the list
            }
            writeToStream(new Object[]{Commands.SHOW_CONTACTS, username, contactsList}); //send contacts to the client
        }

        /**
         * Fetches additional user information from the database and sends it to the client
         * @param userInfoList a list of user information maps fetched from the database
         */
        private void userInfoFromDatabase(List<Map<String, Object>> userInfoList) {
            User user = null;
            for (Map<String, Object> userInfo : userInfoList) {
                user = new User(
                        (String) userInfo.get("username"),
                        (String) userInfo.get("firstname"),
                        (String) userInfo.get("lastname"),
                        (byte[]) userInfo.get("image")
                );
            }
            writeToStream(new Object[]{Commands.LOGIN_SUCCESSFUL, user}); //notify the client of successful login
        }

        /**
         * Starts a chat session between users
         * Notifies all participants to open a chat view
         * @param list a list of usernames in the chat
         */
        public void startChatting(LinkedList<String> list) {
            for (String user : list) {
                if (clients.get(user) != null) {
                    clients.get(user).writeToStream(new Object[]{Commands.START_CHATTING, list}); //notify each user
                }
            }
        }
        /**
         * Updates the list of active users by notifying all connected clients
         * Sends the current list of connected users to each client
         */
        public void updateActiveUsers() {
            String[] connectedUsers = clients.keySet().toArray(new String[0]); //get all connected usernames
            for (ClientHandler clientHandler : clients.values()) {
                clientHandler.writeToStream(new Object[]{Commands.CONNECTED_USER, connectedUsers}); //notify each client
            }
        }

        /**
         * Removes a client from the system
         * Deletes the user from the clients and usersApps mappings
         * @param user the username of the client to remove
         */
        public void removeClient(String user) {
            clients.remove(user); //remove from active clients
            usersApps.remove(user); //remove from user apps
        }

        /**
         * Adds a client to the system
         * Registers the user in the clients mapping
         * @param user the username of the client to add
         * @param clientHandler the ClientHandler associated with the user
         */
        public void put(String user, ClientHandler clientHandler) {
            clients.put(user, clientHandler);
        }

        /**
         * Registers a new chat session between users
         * Adds the chat to the shared chat list in a thread-safe manner
         * @param chat the list of users in the chat
         */
        public void registerChat(LinkedList<String> chat) {
            synchronized (userChats) {
                userChats.add(chat);
            }
        }

        /**
         * Deletes a chat session
         * Removes the chat from the shared chat list in a thread-safe manner
         * @param chat the list of users in the chat to delete
         */
        public void deleteChat(LinkedList<String> chat) {
            synchronized (userChats) {
                userChats.remove(chat);
            }
        }

        /**
         * Sends an object to the client via the output stream
         * Ensures the object is written and flushed properly
         * @param object the object to send
         */
        public void writeToStream(Object object) {
            try {
                oos.writeObject(object);
                oos.flush();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error writing object to stream. Object class " + object.getClass().getName(), e);
            }
        }

        /**
         * Closes all resources associated with a user session
         * Removes the user from the system and closes all streams and sockets
         * @param user the username of the user to close
         */
        public void closeAll(String user) {
            try {
                removeClient(user); //remove the client from the system
                removeFromActiveUsers(user); //notify other users about logout

                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing resources for user: " + user, e);
            }
        }

        /**
         * Removes a user from the active users list
         * Notifies all clients about the removal of the user
         * @param user the username of the user to remove
         * @throws IOException if an I/O error occurs while notifying clients
         */
        public void removeFromActiveUsers(String user) throws IOException {
            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                entry.getValue().writeToStream(new Object[]{Commands.REMOVE_ACTIVE_USER, user}); //notify each client
            }
        }

    }
}
