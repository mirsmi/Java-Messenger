package View;

import Controller.ViewController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Main client application view that handles UI and user interaction
 */
public class MainPage implements Serializable {
    private final ViewController viewController;

    private final HashMap<LinkedList<String>, ChatView> usersChatViews = new HashMap<>();
    private JList<Object> contactList; //displays user's contacts
    private JList<Object> activeUsersList; //displays active users
    private final ArrayList<String> activeUsers = new ArrayList<>();
    private final ArrayList<Object> contacts = new ArrayList<>();
    private JButton startChattingButton;
    private JButton addToContactsButton;
    private JFrame frontPageFrame;

    /**
     * Class constructor
     */
    public MainPage(ViewController viewController){
        this.viewController = viewController;
    }

    /**
     * Sets up the front page of the application
     */
    public void setUpFrontPage(JPanel profilePanel) {
        if (profilePanel == null) {
            throw new IllegalArgumentException("Profile panel cannot be null!");
        }

        String fullName = viewController.getClient().getUser().getFirstName() + " " + viewController.getClient().getUser().getLastName();
        frontPageFrame = new JFrame(fullName);

        initializeLists(); //creates the contact list and active users list
        JPanel mainPanel = buildMainPanel(profilePanel);

        frontPageFrame.add(mainPanel);
        frontPageFrame.pack();
        frontPageFrame.setVisible(true);
        frontPageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setUpWindowListener();
    }

    /**
     * Builds the main front page panel
     */
    private JPanel buildMainPanel(JPanel profilePanel) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(profilePanel, BorderLayout.NORTH);
        mainPanel.add(buildListsPanel(), BorderLayout.CENTER);
        mainPanel.add(buildButtonPanel(), BorderLayout.SOUTH);
        return mainPanel;
    }

    /**
     * Builds the lists panel containing contacts and active users
     */
    private JPanel buildListsPanel() {
        JPanel listPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listPanel.add(buildListPanel("Contacts", contactList));
        listPanel.add(buildListPanel("Active Users", activeUsersList));
        return listPanel;
    }

    /**
     * Builds a labeled panel for a list
     */
    private JPanel buildListPanel(String title, JList<Object> list) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title, JLabel.CENTER);
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the button panel for user actions
     */
    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addToContactsButton);
        buttonPanel.add(startChattingButton);
        return buttonPanel;
    }

    /**
     * Sets up the profile panel at the top of the application
     */
    public JPanel setUpProfilePanel(JLabel profilePicLabel) {
        JPanel profilePanel = new JPanel(new BorderLayout());

        //set up user profile image and name label
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel userNameLabel = new JLabel(viewController.getClient().getUser().getFirstName() + " " + viewController.getClient().getUser().getLastName());
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userInfoPanel.add(profilePicLabel);
        userInfoPanel.add(userNameLabel);

        //logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> viewController.getClient().logout());
        profilePanel.add(userInfoPanel, BorderLayout.WEST);
        profilePanel.add(logoutButton, BorderLayout.EAST);

        return profilePanel;
    }

    /**
     * Initializes the contact and active user lists
     */
    private void initializeLists() {
        contactList = new JList<>(contacts.toArray());
        contactList.setCellRenderer(new ViewController.userImageRenderer());

        activeUsersList = new JList<>(activeUsers.toArray());
        activeUsersList.setCellRenderer(new ViewController.userImageRenderer());
        activeUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        createButtons();
        setUpButtonActions();
    }

    private void createButtons(){
        startChattingButton = new JButton("Chat"); //add chat-button
        addToContactsButton = new JButton("Add to contacts"); //add contact-button
    }

    /**
     * Sets up button listeners for user actions
     */
    private void setUpButtonActions() {
        addToContactsButton.addActionListener(e -> {
            if (!activeUsersList.isSelectionEmpty()) { //adds the selected user to contacts
                viewController.getClient().addToContacts(activeUsersList.getSelectedValues());
                clearSelections(); //clear selected
            }
        });

        startChattingButton.addActionListener(e -> {
            if (!activeUsersList.isSelectionEmpty()) { //start a chat with the selected active user
                viewController.getClient().initiateChat(activeUsersList.getSelectedValues());
            } else if (!contactList.isSelectionEmpty()) {
                viewController.getClient().initiateChat(contactList.getSelectedValues()); //start chat with the selected saved contact
            }
            clearSelections(); //clear selection
        });
    }

    /**
     * Sets up a listener for window close events
     */
    private void setUpWindowListener() { //logout when the user closes the MainPage window
        frontPageFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                viewController.getClient().logout();
            }
        });
    }

    /**
     * Clears selections from contact and active user lists
     */
    private void clearSelections() { //method that clears selection
        contactList.clearSelection();
        activeUsersList.clearSelection();
    }

    /**
     * Logs out the user and cleans up resources
     */
    public void logout() {
        if (frontPageFrame != null) {
            frontPageFrame.dispose();
            usersChatViews.clear();
        }
    }

    //getters
    public JList<Object> getActiveUsersList() {
        return activeUsersList;
    }

    public ArrayList<String> getActiveUsers() {
        return activeUsers;
    }

    public HashMap<LinkedList<String>, ChatView> getUsersChatViews() {
        return usersChatViews;
    }

    public JList<Object> getContactList() {
        return contactList;
    }

    public ArrayList<Object> getContacts() {
        return contacts;
    }
}
