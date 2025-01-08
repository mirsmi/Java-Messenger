package View;

import Controller.ViewController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class representing the chat view in the client application
 */
public class ChatView {
    private final ArrayList<Object> messagesList = new ArrayList<>(); //stores chat messages
    private LinkedList<String> people = new LinkedList<>(); //list of people in the chat
    private JButton sendImageButton;
    private JButton sendMessageButton;
    private JTextField messageTextField; //input field for typing messages
    private JList chatField; //list to display chat messages
    private JFrame chatViewFrame; //main frame for the chat view
    private boolean showingMessages; //flag to determine if messages are being displayed
    private final ViewController viewController;

    //constructor for initializing ChatView with a list of people
    public ChatView(String username, LinkedList<String> people, ViewController viewController) {
        this.people = people;
        this.viewController = viewController;
        showChatView(username);
    }

    //constructor for initializing ChatView without a people list, used to show stored messages sent when the recipient was offline
    public ChatView(ViewController viewController) {
        this.showingMessages = true;
        this.viewController = viewController;
    }

    //sets up and displays the chat view window
    public void showChatView(String username) {
        chatViewFrame = new JFrame("Chat view: " + username);

        //setup components
        setupChatField();
        JPanel mainPanel = setupMainPanel();
        setupWindowCloseBehavior();

        //add main panel to the frame
        chatViewFrame.add(mainPanel);
        chatViewFrame.setSize(600, 400); //slightly larger window
        chatViewFrame.setMinimumSize(new Dimension(500, 350));
        chatViewFrame.setLocationRelativeTo(null); //center the window
        chatViewFrame.setVisible(true);
    }

    //initializes and sets up the chat field for displaying messages
    private void setupChatField() {
        chatField = new JList(messagesList.toArray());
        chatField.setCellRenderer(new ViewController.messageListRenderer());
        chatField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatField.setBackground(new Color(245, 245, 245));
        chatField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //padding around messages
    }

    //creates and sets up the main panel with chat and input components
    private JPanel setupMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); //add gaps between components
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //padding around the entire panel

        setUpMessageFiled(); //setup message input field
        setUpSendButton(); //set up send-button
        setUpImageButton(); //set up image-button

        //add components to the panel
        mainPanel.add(new JScrollPane(chatField), BorderLayout.CENTER);
        mainPanel.add(createSendPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private void setUpMessageFiled(){
        messageTextField = new JTextField(20);
        messageTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageTextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        messageTextField.requestFocusInWindow(); //focus on the input field
    }

    //configures window closing behavior based on the state
    private void setupWindowCloseBehavior() {
        chatViewFrame.setDefaultCloseOperation(showingMessages ? JFrame.DISPOSE_ON_CLOSE : JFrame.DO_NOTHING_ON_CLOSE);
        if (!showingMessages) {
            viewController.closeChatView(chatViewFrame, people);
        }
    }

    //when the user clicks on the send-button
    private void setUpSendButton(){
        sendMessageButton = new JButton("Send");
        sendMessageButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendMessageButton.setBackground(new Color(15, 76, 140));
        sendMessageButton.setForeground(Color.WHITE);
        sendMessageButton.setFocusPainted(false);
        //sends a text message through the client when the button is pressed
        sendMessageButton.addActionListener(e -> viewController.sendText(messageTextField, people));
    }

    //when the user clicks on the image-button
    private void setUpImageButton(){
        sendImageButton = new JButton("Image");
        sendImageButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendImageButton.setBackground(new Color(8, 149, 8));
        sendImageButton.setForeground(Color.WHITE);
        sendImageButton.setFocusPainted(false);
        //opens a file chooser, allowing the user to select an image to send when the button is pressed
        sendImageButton.addActionListener(e -> viewController.sendImage(messageTextField, people));
    }

    //creates the panel containing the message input field and action buttons
    private JPanel createSendPanel() {
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5)); //add gaps between input field and buttons
        sendPanel.setBackground(Color.WHITE);

        //create a panel for buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0)); //horizontal layout for buttons
        buttonPanel.add(sendMessageButton);
        buttonPanel.add(sendImageButton);

        sendPanel.add(messageTextField, BorderLayout.CENTER);
        sendPanel.add(buttonPanel, BorderLayout.EAST);
        return sendPanel;
    }

    //getters
    public ArrayList<Object> getMessagesList() {
        return messagesList;
    }

    public JTextField getMessageTextField() {
        return messageTextField;
    }

    public JList getChatField() {
        return chatField;
    }

    public JFrame getChatViewFrame() {
        return chatViewFrame;
    }
}
