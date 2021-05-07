package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

/*
Клиент, в начале своей работы, должен запросить у пользователя адрес и порт сервера,
подсоединиться к указанному адресу, получить запрос имени от сервера,
спросить имя у пользователя, отправить имя пользователя серверу, дождаться принятия имени сервером.
После этого клиент может обмениваться текстовыми сообщениями с сервером.
Обмен сообщениями будет происходить в двух параллельно работающих потоках.
Один будет заниматься чтением из консоли и отправкой прочитанного серверу,
а второй поток будет получать данные от сервера и выводить их в консоль.
 */
public class Client {
    protected Connection connection;
    //boolean clientConnected будет устанавливаться в true, если клиент подсоединен к серверу или в false в противном случае
    private volatile boolean clientConnected = false;

    //class SocketThread отвечает за поток, устанавливающий сокетное соединение и читающий сообщения сервера

    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }
        //метод clientHandshake() будет представлять клиента серверу
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    String name = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, name));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                } else throw new IOException("Unexpected MessageType");
            }
        }
        //метод clientMainLoop() будет реализовывать главный цикл обработки сообщений сервера
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) processIncomingMessage(message.getData());
                else if (message.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
                else if (message.getType() == MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
                else throw new IOException("Unexpected MessageType");
            }
        }

        public void run() {
            try {
                String serverAddress = getServerAddress();
                int serverPort = getServerPort();
                Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                    clientMainLoop();
                } catch (IOException e) {
                    notifyConnectionStatusChanged(false);
                } catch (ClassNotFoundException e) {
                    notifyConnectionStatusChanged(false);
                }
            }
        }

    /*String getServerAddress() просит ввести адрес сервера у пользователя и возвращает введенное значение.
    Адрес может быть строкой, содержащей ip, если клиент и сервер запущен на
     разных машинах или 'localhost', если клиент и сервер работают на одной машине*/
    protected String getServerAddress() throws IOException {
        ConsoleHelper.writeMessage("Введите адрес сервера (IP или LocaleHost): ");
        return ConsoleHelper.readString();
    }
    //int getServerPort() запрашивает ввод порта сервера и возвращать его
    protected  int getServerPort() throws IOException {
        ConsoleHelper.writeMessage("Введите порт сервера: ");
        return ConsoleHelper.readInt();
    }

    // запрашивает и возвращает имя Пользователя
    protected String getUserName() throws IOException {
        ConsoleHelper.writeMessage("Введите имя: ");
        return ConsoleHelper.readString();
    }

    /* в данной реализации клиента boolean shouldSendTextFromConsole()
       всегда должен возвращать true (мы всегда отправляем текст введенный в консоль)
       Этот метод может быть переопределен, если мы будем писать какой-нибудь другой клиент,
       унаследованный от нашего, который не должен отправлять введенный в консоль текст */
    protected boolean shouldSendTextFromConsole() {
        return true;
    }
    //SocketThread getSocketThread() создаёт и возвращает новый объект класса SocketThread
    protected SocketThread getSocketThread() {
        return new SocketThread();
    }
    //создает новое текстовое сообщение, используя переданный текст и
    // отправляет его серверу через соединение connection
    protected void sendTextMessage(String text) {
        try {
            Message message = new Message(MessageType.TEXT,text);
            connection.send(message);
        } catch (IOException e) {
            this.clientConnected = false;
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        Client client = new Client();
        synchronized (this) {
            try {
                this.wait();
                this.notify();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        if (this.clientConnected == true) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        while (this.clientConnected == true) {
            String string = ConsoleHelper.readString();
            if (string.equals("exit")) break;
            if (shouldSendTextFromConsole() == true) {
                sendTextMessage(string);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.run();
    }
}
