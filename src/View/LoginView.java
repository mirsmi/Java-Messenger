package View;

import Controller.ViewController;

import javax.swing.*;
import java.awt.*;

public class LoginView {
    private final ViewController viewController;

    private final JFrame loginView = new JFrame("Login View");
    private final JTextField usernameTextField = new JTextField(20);
    private final JPasswordField passwordTextField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Login");
    private final JButton registerButton = new JButton("Register");

    /**
     * Constructor for LoginView
     * @param viewController the ViewController instance
     */
    public LoginView(ViewController viewController) {
        this.viewController = viewController;
        initUI();
        setUpListeners();
    }

    /**
     * Initializes the UI components and layout.
     */
    private void initUI() {
        setupLoginView();
        addTitleLabel();
        addUsernameField();
        addPasswordField();
        addLoginButton();
        addRegisterButton();
        loginView.setVisible(true); //set visible after all components are added
    }

    /**
     * Configures the JFrame properties.
     */
    private void setupLoginView() {
        loginView.setSize(450, 350);
        loginView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loginView.setLayout(new GridBagLayout());
        loginView.getContentPane().setBackground(new Color(240, 240, 240));
    }

    /**
     * Adds the title label to the login view.
     */
    private void addTitleLabel() {
        GridBagConstraints gbc = createGridBagConstraints();
        JLabel titleLabel = new JLabel("Java Messenger");
        titleLabel.setFont(titleLabel.getFont().deriveFont(24.0f));
        titleLabel.setForeground(new Color(15, 76, 140));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginView.add(titleLabel, gbc);
    }

    /**
     * Adds the username label and text field to the login view.
     */
    private void addUsernameField() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginView.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameTextField.setBackground(new Color(255, 255, 255));
        loginView.add(usernameTextField, gbc);
    }

    /**
     * Adds the password label and text field to the login view.
     */
    private void addPasswordField() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginView.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordTextField.setBackground(new Color(255, 255, 255));
        loginView.add(passwordTextField, gbc);
    }

    /**
     * Adds the login button to the login view.
     */
    private void addLoginButton() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton.setBackground(new Color(8, 149, 8));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginView.add(loginButton, gbc);
    }

    /**
     * Adds the register button to the login view.
     */
    private void addRegisterButton() {
        GridBagConstraints gbc = createGridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        registerButton.setBackground(new Color(15, 76, 140));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginView.add(registerButton, gbc);
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
     * Sets up listeners for login and register buttons
     */
    private void setUpListeners() {
        loginButton.addActionListener(e -> {
            String username = usernameTextField.getText();
            String password = new String(passwordTextField.getPassword());

            if (!username.isEmpty() && !password.isEmpty()) {
                viewController.login(username, password);
                loginView.dispose();
            } else {
                JOptionPane.showMessageDialog(loginView, "Please enter a valid username and password!");
            }
        });

        registerButton.addActionListener(e -> {
            viewController.showRegisterView();
            loginView.dispose();
        });
    }
}
