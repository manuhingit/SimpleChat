package apps.manuhin.chat.client;

import apps.manuhin.chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Date bot for chat.
 */
public class BotClient extends ConsoleClient {

    public class BotSocketThread extends SocketThread {
        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessageToConsole(message);
            if (message.contains(": ")) {
                String name = message.split(": ")[0];
                String text = message.split(": ")[1];
                SimpleDateFormat df;
                switch (text) {
                    case "date":
                        df = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    case "day":
                        df = new SimpleDateFormat("d");
                        break;
                    case "month":
                        df = new SimpleDateFormat("MMMM");
                        break;
                    case "year":
                        df = new SimpleDateFormat("YYYY");
                        break;
                    case "time":
                        df = new SimpleDateFormat("H:mm:ss");
                        break;
                    case "hour":
                        df = new SimpleDateFormat("H");
                        break;
                    case "minutes":
                        df = new SimpleDateFormat("m");
                        break;
                    case "seconds":
                        df = new SimpleDateFormat("s");
                        break;
                    default:
                        df = null;
                        break;
                }
                if (df != null) {
                    sendTextMessage("Информация для " + name + ": " + df.format(Calendar.getInstance().getTime()));
                }
            }
        }

        @Override
        void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hello everybody!. I'm a date bot. My commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }
    }

    public static void main(String[] args) {
        BotClient bot = new BotClient();
        bot.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%d", (int) (Math.random() * 100));
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }
}
