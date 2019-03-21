package apps.manuhin.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class that helps with console interaction.
 */
public class ConsoleHelper {
    private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessageToConsole(String message) {
        System.out.println(message);
    }

    public static String readStringFromConsole() {
        while (true) {
            try {
                return consoleReader.readLine();
            }  catch (IOException e) {
                writeMessageToConsole("There an error while reading an string. Please try again.");
            }
        }
    }

    public static int readIntFromConsole() {
        while (true) {
            try {
                return Integer.parseInt(readStringFromConsole());
            }  catch (NumberFormatException e) {
                writeMessageToConsole("There an error while reading an integer. Please try again.");
            }
        }
    }
}
