package apps.manuhin.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that implements Server logic.
 */
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private static final int PORT = 8888;

    /**
     * Launching server and handling incoming connections.
     *
     * @param args - program arguments.
     */
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            ConsoleHelper.writeMessageToConsole("Cannot run server on port " + PORT);
            System.exit(-1);
        }

        ConsoleHelper.writeMessageToConsole("Server is running on port " + PORT);
        ConsoleHelper.writeMessageToConsole(serverSocket.getLocalSocketAddress().toString());

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientSocketHandler clientSocketHandler = new ClientSocketHandler(clientSocket);
                clientSocketHandler.start();
            }
        } catch (IOException e) {
            try {
                serverSocket.close();
            } catch (IOException e1) {
                ConsoleHelper.writeMessageToConsole("Cannot properly close the server clientSocket.");
                e1.printStackTrace();
            }
            ConsoleHelper.writeMessageToConsole("Cannot handle incoming clientSocket, message: " + e.getMessage());
        }
    }

    /**
     * Send message to all connected client sockets.
     *
     * @param message that should be sent to all clients.
     */
    private static void sendBroadcastMessage(Message message) {
        try {
            for (Connection connection : connectionMap.values()) connection.sendMessage(message);
        } catch (Exception e) {
            ConsoleHelper.writeMessageToConsole("Cannot sendMessage a broadcasting message.");
        }
    }

    /**
     * Class that should handle incoming sockets.
     */
    private static class ClientSocketHandler extends Thread {
        private Socket clientSocket;

        ClientSocketHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        /**
         * Establish connection, handshake and broadcast name of client to other clients.
         * Remove user when disconnect and broadcast that to other clients.
         */
        @Override
        public void run() {
            ConsoleHelper.writeMessageToConsole("Established connection with address: " + clientSocket.getRemoteSocketAddress().toString());
            String name = null;
            try (Connection connection = new Connection(clientSocket)) {
                name = serverHandshakeWithClient(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);
            } catch (Exception e) {
                ConsoleHelper.writeMessageToConsole("There is an error when trying to establish connection with remote client.");
                e.printStackTrace();
            } finally {
                if (name != null) {
                    connectionMap.remove(name);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                }
            }
        }

        /**
         * Perform handshake with remote client and try to get his name.
         *
         * @param connection with remote client.
         * @return name of the client.
         * @throws IOException            - something wrong with connection.
         * @throws ClassNotFoundException - cannot receive name of the user.
         */
        private String serverHandshakeWithClient(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.sendMessage(new Message(MessageType.NAME_REQUEST, "Please enter a username."));
                Message answer = connection.receive();
                if (answer.getType() == MessageType.USER_NAME) {
                    String name = answer.getData();
                    if (name.length() > 0 && !connectionMap.containsKey(name)) {
                        connectionMap.put(name, connection);
                        connection.sendMessage(new Message(MessageType.NAME_ACCEPTED));
                        return name;
                    }
                }
            }
        }

        /**
         * Send a message about the new user to all users except an added one.
         *
         * @param connection with remote client.
         * @param userName   of the user.
         * @throws IOException - something wrong with connection.
         */
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : connectionMap.keySet())
                if (!name.equals(userName))
                    connection.sendMessage(new Message(MessageType.USER_ADDED, name));
        }

        /**
         * Checking for messages from client.
         *
         * @param connection with remote client.
         * @param userName   of the user.
         * @throws IOException            - something wrong with connection.
         * @throws ClassNotFoundException - cannot receive name of the user.
         */
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT)
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                else
                    ConsoleHelper.writeMessageToConsole("Wrong message type from the client " + userName + ". Type - " + message.getType());
            }
        }
    }

}
