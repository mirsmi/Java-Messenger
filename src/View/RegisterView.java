package View;

import Controller.ViewController;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class RegisterView {
    private final ViewController viewController;

    private File profileImageFile;
    private final JFrame registerView = new JFrame("Register View");
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField usernameTextField = new JTextField(20);
    private final JTextField firstNameField = new JTextField(20);
    private final JTextField lastNameField = new JTextField(20);
    private final JButton imageButton = new JButton("Select Image");
    private final JButton registerButton = new JButton("Register");

    /**
     * Constructor for RegisterView
     * @param viewController the ViewController instance
     */
    public RegisterView(ViewController viewController) {
        this.viewController = viewController;
        initUI();
        setUpListeners();
    }

    /**
     * Initializes the UI components and layout.
     */
    private void initUI() {
        setupRegisterView();
        addTitleLabel();
        addUsernameField();
        addProfileImageButton();
        addPasswordField();
        addFirstNameField();
        addLastNameField();
        addRegisterButton();
        registerView.setVisible(true);
    }

    /**
     * Configures the JFrame properties.
     */
    private void setupRegisterView() {
        registerView.setSize(400, 450);
        registerView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerView.setLayout(new GridBagLayout());
        registerView.getContentPane().setBackground(new Color(240, 240, 240));
    }

    /**
     * Adds the title label to the registration view.
     */
    private void addTitleLabel() {
        GridBagConstraints gbc = createGridBagConstraints();
        JLabel titleLabel = new JLabel("Registration");
        titleLabel.setFont(titleLabel.getFont().deriveFont(24.0f));
        titleLabel.setForeground(new Color(15, 76, 140));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerView.add(titleLabel, gbc);
    }

    /**
     * Adds the username label and text field to the registration view.
     */
    private void addUsernameField() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerView.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameTextField.setBackground(new Color(255, 255, 255));
        registerView.add(usernameTextField, gbc);
    }

    /**
     * Adds the profile image button to the registration view.
     */
    private void addProfileImageButton() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerView.add(new JLabel("Profile Image:"), gbc);
        gbc.gridx = 1;
        imageButton.setBackground(new Color(15, 76, 140));
        imageButton.setForeground(Color.WHITE);
        registerView.add(imageButton, gbc);
    }

    /**
     * Adds the password label and text field to the registration view.
     */
    private void addPasswordField() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerView.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField.setBackground(new Color(255, 255, 255));
        registerView.add(passwordField, gbc);
    }

    /**
     * Adds the first name label and text field to the registration view.
     */
    private void addFirstNameField() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        registerView.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        firstNameField.setBackground(new Color(255, 255, 255));
        registerView.add(firstNameField, gbc);
    }

    /**
     * Adds the last name label and text field to the registration view.
     */
    private void addLastNameField() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        registerView.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        lastNameField.setBackground(new Color(255, 255, 255));
        registerView.add(lastNameField, gbc);
    }

    /**
     * Adds the register button to the registration view.
     */
    private void addRegisterButton() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton.setBackground(new Color(8, 149, 8));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerView.add(registerButton, gbc);
    }

    /**
     * Creates a base GridBagConstraints object with common settings.
     *
     * @return a pre-configured GridBagConstraints instance
     */
    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        return gbc;
    }

    /**
     * Sets up listeners for image and register buttons
     */
    private void setUpListeners() {
        imageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int choice = fileChooser.showOpenDialog(registerView);
            if (choice == JFileChooser.APPROVE_OPTION) {
                profileImageFile = fileChooser.getSelectedFile();
            }
        });

        registerButton.addActionListener(e -> {
            if (viewController.isInputValid(profileImageFile, usernameTextField, passwordField, firstNameField, lastNameField)) {
                viewController.register(
                        usernameTextField.getText(),
                        new String(passwordField.getPassword()),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        profileImageFile.getAbsolutePath()
                );
                registerView.dispose();
            } else {
                JOptionPane.showMessageDialog(registerView, "Invalid input! Please fill in all of the fields and select a valid image.");
            }
        });
    }

}
