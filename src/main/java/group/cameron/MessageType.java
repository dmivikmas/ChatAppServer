package group.cameron;

/**
 * Enumeration of different types of messages that can be exchanged between the client and the server.
 */
public enum MessageType {
    NAME_REQUEST,
    USER_NAME,
    NAME_ACCEPTED,
    TEXT,
    USER_ADDED,
    USER_REMOVED
}
