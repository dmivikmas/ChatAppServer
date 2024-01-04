package group.cameron;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Class for managing a connection between a client and a server.
 * It facilitates sending and receiving messages through a network connection.
 */
public class Connection implements Closeable {

    private final Socket socket;
    private final ObjectOutputStream out;

    private final ObjectInputStream in;

    /**
     * Constructs a new connection instance.
     *
     * @param socket the socket for the connection
     * @throws IOException if an I/O error occurs while creating the output and input streams
     */
    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Sends a message through the connection.
     *
     * @param message the message to be sent
     * @throws IOException if an I/O error occurs while writing the message
     */
    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    /**
     * Receives a message through the connection.
     *
     * @return the received message
     * @throws IOException if an I/O error occurs while reading the message
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (in) {
            return (Message) in.readObject();
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    /**
     * Closes this connection and releases any system resources associated with it.
     *
     * @throws IOException if an I/O error occurs while closing the connection
     */
    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}
