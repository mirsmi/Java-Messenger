package Model;

import javax.swing.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a chat message in the system
 * A message can include text, an image, or both
 */
public class Message implements Serializable {

    private final String text;
    private final ImageIcon image;
    private final String sentBy;
    private final List<String> recipientList;
    private final String sendTime;
    private final String imagePath;

    /**
     * Private constructor to enforce the use of the Builder
     * @param builder builder instance
     */
    private Message(Builder builder) {
        this.text = builder.text;
        this.image = builder.image;
        this.sentBy = builder.sentBy;
        this.recipientList = Collections.unmodifiableList(builder.recipientList);
        this.sendTime = builder.sendTime;
        this.imagePath = builder.imagePath;
    }

    /**
     * Gets the text content of the message
     * @return the text content
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the image associated with the message
     * @return the image
     */
    public ImageIcon getImage() {
        return image;
    }

    /**
     * Gets the sender of the message
     * @return the sender
     */
    public String getSentBy() {
        return sentBy;
    }

    /**
     * Gets an unmodifiable view of the recipient list
     * @return an unmodifiable list of recipients
     */
    public List<String> getRecipientList() {
        return recipientList;
    }

    /**
     * Gets the time the message was sent
     * @return the send time
     */
    public String getSendTime() {
        return sendTime;
    }

    /**
     * Gets the file path of the image associated with the message
     * @return the image file path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Builder class for constructing a Message object
     */
    public static class Builder {
        private String text;
        private ImageIcon image;
        private String sentBy;
        private final LinkedList<String> recipientList = new LinkedList<>();
        private String sendTime;
        private String imagePath;

        /**
         * Sets the text content of the message
         * @param text the text content
         * @return the Builder instance
         */
        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the image associated with the message
         * @param image the image
         * @return the Builder instance
         */
        public Builder withImage(ImageIcon image) {
            this.image = image;
            return this;
        }

        /**
         * Sets the sender of the message
         * @param sentBy the sender
         * @return the Builder instance
         */
        public Builder withSentBy(String sentBy) {
            this.sentBy = sentBy;
            return this;
        }

        /**
         * Adds multiple recipients to the recipient list
         * @param recipients a list of recipients
         * @return the Builder instance
         */
        public Builder addRecipients(List<String> recipients) {
            this.recipientList.addAll(recipients);
            return this;
        }

        /**
         * Sets the time the message was sent
         * @param sendTime the send time
         * @return the Builder instance
         */
        public Builder withSendTime(String sendTime) {
            this.sendTime = sendTime;
            return this;
        }

        /**
         * Sets the file path of the image associated with the message
         * @param imagePath the image file path
         * @return the Builder instance
         */
        public Builder withImagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        /**
         * Builds and returns the Message instance
         * @return a new Message instance
         */
        public Message build() {
            return new Message(this);
        }
    }
}
