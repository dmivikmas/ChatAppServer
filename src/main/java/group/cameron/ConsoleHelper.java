package group.cameron;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utility class for console operations. It provides methods for reading from and writing to the console.
 */
public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Writes a message to the console.
     *
     * @param message The message to be written to the console.
     */
    public static void writeMessage(String message) {
        System.out.println(message);
    }

    /**
     * Reads a string from the console. In case of an I/O error, the user is prompted to try again.
     *
     * @return The string read from the console.
     */
    public static String readString() {
        while (true) {
            try {
                return reader.readLine();
            } catch (IOException ignore) {
                System.out.println("An error occurred while trying to enter text. Please try again.");
            }
        }
    }

    /**
     * Reads an integer from the console. In case of a format error, the user is prompted to try again.
     *
     * @return The integer read from the console.
     */
    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException ignore) {
                System.out.println("An error occurred while trying to enter a number. Please try again.");
            }
        }
    }

}
