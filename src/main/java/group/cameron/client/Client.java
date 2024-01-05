package group.cameron.client;

import group.cameron.Connection;
import group.cameron.ConsoleHelper;
import group.cameron.Message;
import group.cameron.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Client class for handling chat operations. It manages the client-side operations for connecting
 * to the chat server, handling messages, and user interactions.
 */
public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;

    /**
     * Requests and returns the server address from the user.
     *
     * @return The server address as a string.
     */
    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address: ");
        return ConsoleHelper.readString();
    }

    /**
     * Requests and returns the server port from the user.
     *
     * @return The server port as an integer.
     */
    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port: ");
        return ConsoleHelper.readInt();
    }

    /**
     * Requests and returns the username from the user.
     *
     * @return The username as a string.
     */
    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter your username: ");
        return ConsoleHelper.readString();
    }

    /**
     * Determines if the client should send text from the console. In this implementation, it always returns true.
     *
     * @return Always returns true.
     */
    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    public class SocketThread extends Thread {

        /**
         * Processes incoming messages and displays them to the user.
         *
         * @param message The incoming message as a string.
         */
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        /**
         * Informs the user about a new user joining the chat.
         *
         * @param userName The username of the user who joined.
         */
        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has joined the chat.");
        }

        /**
         * Informs the user about a user leaving the chat.
         *
         * @param userName The username of the user who left.
         */
        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has left the chat.");
        }

        /**
         * Updates the connection status and notifies the main thread.
         *
         * @param clientConnected The new status of the client connection.
         */
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        /**
         * Performs the client handshake with the server.
         *
         * @throws IOException
         * @throws ClassNotFoundException
         */
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    String name = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, name));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        /**
         * The main loop for handling messages from the server.
         *
         * @throws IOException
         * @throws ClassNotFoundException
         */
        protected void clientMainLoop() throws IOException, ClassNotFoundException {

            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        /**
         * The main execution method for the client thread. It establishes a socket connection to the server,
         * initiates the client handshake, and enters the main loop for processing server messages.
         */
        @Override
        public void run() {
            String host = getServerAddress();
            int port = getServerPort();

            try {
                Socket socket = new Socket(host, port);
                connection = new Connection(socket);

                clientHandshake();

                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    /**
     * Creates and returns a new SocketThread instance.
     *
     * @return A new instance of SocketThread.
     */
    protected SocketThread getSocketThread() {
        SocketThread socketThread = new SocketThread();
        return socketThread;
    }

    /**
     * Sends a text message to the server.
     *
     * @param text The text message to be sent.
     */
    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Error while sending message: " + e.getMessage());
            clientConnected = false;
        }
    }

    /**
     * The main execution method for the client. This method starts the socket thread to handle communication
     * with the server, waits for a connection to be established, and then processes user input for chat messages.
     */
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Error while waiting for a response from the server.");
                return;
            }
        }

        if (clientConnected) {
            ConsoleHelper.writeMessage("Connection established. Type 'exit' to quit.");
        } else {
            ConsoleHelper.writeMessage("An error occurred during client operation.");
        }

        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if ("exit".equals(text)) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(text);
            }
        }
    }

    /**
     * The main method to start the client application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
