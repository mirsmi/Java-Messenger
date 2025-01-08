package Controller;

import Model.Message;
import Model.User;
import View.MainPage;
import View.RegisterView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * View Controller class that handles the View logic
 */
public class ViewController {
    private final MainPage mainPage;
    private Client client;
    private static final Logger logger = Logger.getLogger(ViewController.class.getName());

    public ViewController(){
        mainPage = new MainPage(this);
    }

    /**
     * Handles the logic that shows the front page when the user logs in / registers
     */
    public void showFrontPage(User user, boolean newUser){
        JLabel profilePicLabel = new JLabel();

        if(newUser) {
            String imagePath = user.getImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    File imgFile = new File(imagePath);
                    Image img = ImageIO.read(imgFile);

                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                        ImageIcon profilePic = new ImageIcon(scaledImg);
                        profilePicLabel.setIcon(profilePic);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Something is wrong with the image path!", e);
                }
            }
        }
        else{
            byte[] imageBytes = user.getImageByte();
            if (imageBytes != null && imageBytes.length > 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    BufferedImage img = ImageIO.read(bis);
                    Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);

                    ImageIcon imageIcon = new ImageIcon(scaledImg);
                    profilePicLabel.setIcon(imageIcon);
                }
                catch (IOException e) {
                    logger.log(Level.SEVERE, "Something is wrong with the image!", e);
                }
            }
        }

        mainPage.setUpFrontPage(mainPage.setUpProfilePanel(profilePicLabel));
    }

    /**
     * Creates a new Client if username and password are not null
     * @param username username
     * @param password password
     */
    public void login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password must be provided.");
        }
        client = new Client(this, mainPage, username, password);
    }

    /**
     * Registers a new user with provided details.
     */
    public void register(String username, String password, String firstName, String lastName, String imagePath) {
        if (username == null || password == null || firstName == null || lastName == null || imagePath == null) {
            throw new IllegalArgumentException("All registration fields must be provided.");
        }
        client = new Client(mainPage, this, username, password, firstName, lastName, imagePath);
    }

    /**
     * Validates the user input
     * @return true if input is valid, false otherwise
     */
    public boolean isInputValid(File profileImageFile, JTextField usernameTextField, JPasswordField passwordField, JTextField firstNameField, JTextField lastNameField) {
        return profileImageFile != null &&
                !usernameTextField.getText().isEmpty() &&
                passwordField.getPassword().length > 0 &&
                !firstNameField.getText().isEmpty() &&
                !lastNameField.getText().isEmpty() &&
                profileImageFile.getAbsolutePath().matches(".*\\.(jpg|jpeg|png)$");
    }


    /**
     * Sends an image in chat
     * @param messageTextField the text field
     * @param people people in the chat
     */
    public void sendImage(JTextField messageTextField, LinkedList<String> people){
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            //resize the selected image
            ImageIcon imageIcon = new ImageIcon(filePath);
            Image resizedImage = imageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(resizedImage);

            //send the image with the current message
            client.sendMessage(messageTextField.getText(), imageIcon, people, filePath);
        }

        messageTextField.requestFocusInWindow(); //refocus on input field
    }

    /**
     * Closes the chat view
     * @param chatViewFrame ChatView frame
     * @param people people in the chat
     */
    public void closeChatView(JFrame chatViewFrame, LinkedList<String> people){
        chatViewFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.closeChatViewForUsers(people);
            }
        });
    }

    /**
     * Method that gets called when the user clicks on the send button
     * @param messageTextField the message field
     * @param people people in chat
     */
    public void sendText(JTextField messageTextField, LinkedList<String> people){
        new Thread(() -> {
            String message = messageTextField.getText();
            if (!message.isEmpty()) {
                client.sendMessage(message, null, people, null);
                messageTextField.setText(""); //clear the text field after sending
            }
            messageTextField.requestFocusInWindow(); //refocus on input field
        }).start();
    }

    /**
     * Helper class that renders messages in the chat view
     */
    public static class messageListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object object, int i, boolean selected, boolean focus) {
            JPanel panel = new JPanel(new BorderLayout());
            JLabel textLabel = new JLabel();
            JLabel iconLabel = new JLabel();

            if (object instanceof Message message) {
                textLabel.setText("(" + message.getSendTime() + ") " + message.getSentBy() + ": " + message.getText());
                iconLabel.setIcon(message.getImage());
            }

            panel.add(textLabel, BorderLayout.CENTER);
            panel.add(iconLabel, BorderLayout.EAST);

            if (selected) {
                panel.setBackground(list.getSelectionBackground());
                textLabel.setForeground(list.getSelectionForeground());
            }
            else {
                panel.setBackground(list.getBackground());
                textLabel.setForeground(list.getForeground());
            }

            return panel;
        }
    }

    /**
     * Helper class that renders user items in the lists
     */
    public static class userImageRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object object, int i, boolean selected, boolean focus) {
            JLabel jLabel = (JLabel) super.getListCellRendererComponent(list, object, i, selected, focus);
            if (object instanceof User user) {
                jLabel.setText(user.getUsername());
            }
            return jLabel;
        }
    }

    /**
     * Gets called when the user clicks on the Register-button
     */
    public void showRegisterView() {
        new RegisterView(this);
    }

    /**
     * Get method for the
     * @return the client variable
     */
    public Client getClient() {
        return client;
    }

}
