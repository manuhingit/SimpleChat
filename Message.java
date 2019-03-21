package apps.manuhin.chat;

import java.io.Serializable;

/**
 * Message which used to sendMessage info from server to client and vice versa.
 */
public class Message implements Serializable {
    private final MessageType type;
    private final String data;

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    /**
     * @return type of message.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @return data of message.
     */
    public String getData() {
        return data;
    }
}
