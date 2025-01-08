# Java Messenger
A client-server chat program built in Java, following the MVC design pattern for clear separation of concerns and maintainability. The program implements the Builder pattern for Message class and the Singleton pattern for managing database operations.

## Features
### User Management
- User Registration: New users can register by providing: username (unique identifier), profile image, first and last name, password (securely hashed using MD5)
- User Login: Existing users can log in securely using their username and password.
- Password Security: Passwords are hashed using the Encryptor class, which employs the MD5 algorithm to ensure secure storage in the database.

### Messaging Functionality
- Real-Time Chat: Users can chat with other connected users in real-time.
- Group Chat: Join group conversations with multiple participants.
- Saved Contacts: Users can send messages to their saved contacts.
- Text and Image Messages: send plain text messages and image messages.

### Server Capabilities
- The server handles multiple connections simultaneously, ensuring smooth communication for all users.
- User data is securely stored in a PostgreSQL database managed through pgAdmin.
- The Singleton pattern is used in the DatabaseManager class to ensure a single instance handles all database interactions.
- Sensitive information, including database credentials, is omitted from the codebase to ensure security.

## Technologies, Patterns and Dependencies Used
- Java: Core programming language.
- PostgreSQL: Database for secure storage of user information.
- PostgreSQL Driver: postgresql-42.7.4.jar.
- MVC Design Pattern: Ensures clear separation between the user interface, business logic, and data management.
- Builder Pattern: Streamlines the creation of message objects with optional attributes.
- Singleton Pattern: Guarantees a single instance of the DatabaseManager class, managing database connections and queries efficiently.
- MD5 Password Hashing: Utilizes the Encryptor class to hash passwords securely before storing them in the database.

## Please Note
You will need to create your own database tables to run this application, as I have removed mine for security reasons. Ensure that your database schema aligns with the structure expected by the program (e.g., tables for users, messages, etc.).

### How to Run
1. Clone the repository to your local machine.
2. Download the postgresql.jar file and add it to your project's library settings in your IDE (e.g., IntelliJ, Eclipse).
3. Configure the database connection by adding your PostgreSQL credentials.
4. Run the Main controller to start the application.
