package Controller;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for encrypting passwords
 */
public class Encryptor {
    private static final Logger logger = Logger.getLogger(Encryptor.class.getName());

    /**
     * Encrypts a password using MD5 hashing
     * @param password the plaintext password to encrypt
     * @return the hashed password as a hexadecimal string
     */
    public static String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger bigInteger = new BigInteger(1, messageDigest);
            return bigInteger.toString(16);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Hashing algorithm not found", e);
            throw new RuntimeException();
        }
    }
}
