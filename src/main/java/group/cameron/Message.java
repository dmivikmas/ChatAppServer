package group.cameron;

import java.io.Serializable;

/**
 * Class responsible for messages being sent. It encapsulates the type of the message and its data.
 */
public class Message implements Serializable {
    private final MessageType type;
    private final String data;

    /**
     * Constructs a message with the specified type and no data.
     *
     * @param type The type of the message.
     */
    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    /**
     * Constructs a message with the specified type and data.
     *
     * @param type The type of the message.
     * @param data The data of the message.
     */
    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the type of the message.
     *
     * @return The type of the message.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Returns the data of the message.
     *
     * @return The data of the message, or null if there is no data.
     */
    public String getData() {
        return data;
    }
}
