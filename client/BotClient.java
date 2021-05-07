package com.javarush.task.task30.task3008.client;
import com.javarush.task.task30.task3008.ConsoleHelper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client{

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() throws IOException {
        return "date_bot_"+(int) (Math.random()*100);
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(": ")) {
            String[] messageArray = message.split(": ");
            Calendar calendar = new GregorianCalendar();
            Date date = calendar.getTime();
            SimpleDateFormat dateFormat;
            switch (messageArray[1].toLowerCase()) {
                case ("дата"):
                    dateFormat = new SimpleDateFormat("d.MM.YYYY");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
                case ("день"):
                    dateFormat = new SimpleDateFormat("d");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
                case ("месяц"):
                    dateFormat = new SimpleDateFormat("MMMM");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
                case ("год"):
                    dateFormat = new SimpleDateFormat("YYYY");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
                case ("время"):
                    dateFormat = new SimpleDateFormat("H:mm:ss");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
                case ("час"):
                    dateFormat = new SimpleDateFormat("H");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
                case ("минуты"):
                    dateFormat = new SimpleDateFormat("m");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
                case ("секунды"):
                    dateFormat = new SimpleDateFormat("s");
                    sendTextMessage("Информация для " + messageArray[0] + ": " + dateFormat.format(date));
                    break;
            }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
