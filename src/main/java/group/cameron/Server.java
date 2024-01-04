package group.cameron;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main server class. It handles incoming client connections and facilitates
 * communication between clients.
 */
public class Server {

    // Maps client names to their respective connections
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    /**
     * The main method to start the server. It initializes the server on a specified port and listens for client connections.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {

        ConsoleHelper.writeMessage("Enter server port:");
        int port = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Server started");

            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Server error: " + e.getMessage());
        }
    }

    /**
     * Sends a broadcast message to all clients connected to the server.
     *
     * @param message The message to be broadcasted.
     */
    public static void sendBroadcastMessage(Message message) {
        connectionMap.values().forEach(connection -> {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Error sending message: " + e.getMessage());
            }
        });
    }

    /**
     * A private inner class to handle client connection threads. Each client connection is handled in a separate thread.
     */
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Manages the server handshake process with the client to establish a connection.
         *
         * @param connection The connection with the client.
         * @return The username of the client.
         * @throws IOException, ClassNotFoundException
         */
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();

                if (message.getType() == MessageType.USER_NAME) {
                    String userName = message.getData();

                    if (userName != null && !userName.isEmpty() && !connectionMap.containsKey(userName)) {
                        connectionMap.put(userName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return userName;
                    }
                }
            }
        }

        /**
         * Notifies a new client about existing clients.
         *
         * @param connection The connection to the new client.
         * @param userName The username of the new client.
         * @throws IOException
         */
        private void notifyUsers(Connection connection, String userName) throws IOException {

            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                String name = entry.getKey();

                if (!name.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));
                }

            }
        }

        /**
         * The main loop for handling server messages. It receives messages from a client and broadcasts them.
         *
         * @param connection The connection with the client.
         * @param userName The username of the client.
         * @throws IOException, ClassNotFoundException
         */
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.TEXT) {
                    String text = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, text));
                } else {
                    ConsoleHelper.writeMessage("Error: Message from " + userName + " is not text");
                }
            }
        }

        /**
         * The main execution method for the client handler thread. It handles the client connection from start to finish.
         */
        @Override
        public void run() {
            ConsoleHelper.writeMessage("New connection established with " + socket.getRemoteSocketAddress());
            String userName = null;

            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error exchanging data with remote address: " + socket.getRemoteSocketAddress());
            } finally {
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }

            ConsoleHelper.writeMessage("Connection with remote address closed: " + socket.getRemoteSocketAddress());
        }
    }
}
