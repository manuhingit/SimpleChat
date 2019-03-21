package apps.manuhin.chat.client.ClientGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ClientGuiView {
    private final ClientGuiController controller;

    private JFrame frame = new JFrame("Java Chat - v1.0");
    private JTextField textField = new JTextField(50);
    private JTextArea messages = new JTextArea(25, 50);
    private JTextArea users = new JTextArea(10, 10);

    ClientGuiView(ClientGuiController controller) {
        this.controller = controller;
        initView();
    }

    private void initView() {
        textField.setEditable(false);
        messages.setEditable(false);
        users.setEditable(false);

        frame.setSize(new Dimension(640, 480));

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);

        frame.getContentPane().add(textField, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(messages), BorderLayout.WEST);
        frame.getContentPane().add(new JScrollPane(users), BorderLayout.EAST);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        textField.addActionListener(e -> {
            if (!textField.getText().isEmpty()) {
                controller.sendTextMessage(textField.getText());
                textField.setText("");
            }
        });
    }

    String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Please enter a server address:",
                "Client configuration",
                JOptionPane.QUESTION_MESSAGE);
    }

    String getUserName() {
        return JOptionPane.showInputDialog(
                frame,
                "Please enter your username:",
                "Client configuration",
                JOptionPane.QUESTION_MESSAGE);
    }

    void notifyConnectionStatusChanged(boolean clientConnected) {
        textField.setEditable(clientConnected);
        if (clientConnected) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Connection with server has successfully established.",
                    "Connection info",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Cannot establish connection with server.",
                    "Connection info",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    void refreshMessages() {
        messages.append(controller.getModel().getNewMessage() + "\n");
    }

    void refreshUsers() {
        ClientGuiModel model = controller.getModel();
        StringBuilder sb = new StringBuilder();
        for (String userName : model.getAllUserNames()) {
            sb.append(userName).append("\n");
        }
        users.setText(sb.toString());
    }
}
