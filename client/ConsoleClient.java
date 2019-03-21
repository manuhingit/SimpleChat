package apps.manuhin.chat.client;

import apps.manuhin.chat.Connection;
import apps.manuhin.chat.ConsoleHelper;
import apps.manuhin.chat.Message;
import apps.manuhin.chat.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Class that implements client logic.
 */
public class ConsoleClient {

    private Connection connection;
    private volatile boolean clientConnected = false;

    /**
     * Daemon that performs net actions such as establishing connection, checking for server messages.
     */
    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessageToConsole(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessageToConsole("[+] " + userName + " has connected.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessageToConsole("[-]" + userName + " has disconnected.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            ConsoleClient.this.clientConnected = clientConnected;
            synchronized (ConsoleClient.this) {
                ConsoleClient.this.notify();
            }
        }

        /**
         * Performs handshake with server.
         *
         * @throws IOException console input error.
         * @throws ClassNotFoundException connection error.
         */
        void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST)
                    connection.sendMessage(new Message(MessageType.USER_NAME, getUserName()));
                else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else throw new IOException("Unexpected MessageType");
            }
        }

        /**
         * Checking for messages from server.
         *
         * @throws IOException console output error.
         * @throws ClassNotFoundException connection error.
         */
        void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) processIncomingMessage(message.getData());
                else if (message.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
                else if (message.getType() == MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
                else throw new IOException("Unexpected MessageType");
            }
        }

        /**
         * Establishing connection with server.
         */
        public void run() {
            try {
                String[] serverAddressSplit = getServerAddress().split(":");
                connection = new Connection(new Socket(serverAddressSplit[0], Integer.valueOf(serverAddressSplit[1])));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    /**
     * Running client app
     *
     * @param args program arguments.
     */
    public static void main(String[] args) {
        ConsoleClient client = new ConsoleClient();
        client.run();
    }

    /**
     * Runs daemon and checks results of establishing connection with server.
     */
    public void run() {
        SocketThread clientSocketThread = getSocketThread();
        clientSocketThread.setDaemon(true);
        clientSocketThread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessageToConsole("Something went wrong while trying to connect to server, exiting...");
                return;
            }
        }

        if (clientConnected) ConsoleHelper.writeMessageToConsole("Connection established. For close app type 'exit'.");
        else {
            ConsoleHelper.writeMessageToConsole("Something went wrong while trying to connect to server, exiting...");
            return;
        }

        // Loop for handling client input
        while (clientConnected) {
            String message = ConsoleHelper.readStringFromConsole();
            if (message.equals("exit")) break;
            else if (shouldSendTextFromConsole()) sendTextMessage(message);
        }
    }


    /**
     * @return address of server.
     */
    protected String getServerAddress() {
        ConsoleHelper.writeMessageToConsole("Please enter a server address: ");
        return ConsoleHelper.readStringFromConsole();
    }

    /**
     * @return username.
     */
    protected String getUserName() {
        ConsoleHelper.writeMessageToConsole("Please enter your username: ");
        return ConsoleHelper.readStringFromConsole();
    }

    /**
     * @return Indicate that client can send messages via console.
     */
    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    /**
     * Sends message to the server.
     *
     * @param text that should be sent to the server.
     */
    public void sendTextMessage(String text) {
        try {
            connection.sendMessage(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessageToConsole("Произошла ошибка при попытке отправить текст.");
            clientConnected = false;
        }
    }
}