package Model;
import java.io.Serializable;

/**
 * Represents a User in the system
 */
public class User implements Serializable {
    private final String username; //unique identifier for the user
    private String firstName;
    private String lastName;
    private String password;
    private boolean isOnline;
    private String imagePath;
    private byte[] imageByte;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public User(String username, String firstName, String lastName, byte[] imageByte){
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageByte = imageByte;
    }

    public User(String username, String password, String firstName, String lastName, String imagePath){
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imagePath = imagePath;
    }

    // Getters and setters for each field
    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    /**
     * @return the user's password (hashed)
     */
    public String getPassword() {
        return password;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getImagePath() {
        return imagePath;
    }

    public byte[] getImageByte() {
        return imageByte;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User other = (User) obj;
        return username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
