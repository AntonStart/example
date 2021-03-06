package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    // Карта класса сервер хранит имя клиента и его подключение
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    // метод отправляет сообщение для всех участников в чате (широковещательное сообщение)
    public static void sendBroadcastMessage(Message message) {
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("сообщение не отправлено");
            }
        }
    }
    // внутренний класс ОБРАБОТЧИК
    private static class Handler extends Thread{

        private Socket socket;
        // run()
        //1 выводит в консоль сообщение об установленном соединении
        //2 создаёт объект класса Соединение на основе сокета
        //3 знакомит сервер и клиента через метод рукопожатия serverHandshake(connection)
        //4 отправляет сообщение всем участникам чата о присоединении нового участника
        //
        @Override
        public void run() {
            ConsoleHelper.writeMessage("A connection to a remote address has been established" + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)) {
                String userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // конструктор с объявлением socket'а
        public Handler(Socket socket) {
            this.socket = socket;
        }

        // метод рукопожатие сервера и участника
        //
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST, "Введите имя:"));
                Message message = connection.receive();
                if ((message.getType() == MessageType.USER_NAME)
                        && (!message.getData().equals(""))
                        && (!message.getData().isEmpty())
                        && (!message.getData().equals(null))
                        && (!connectionMap.containsKey(message.getData())))
                {
                    connectionMap.put(message.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return message.getData();
                }
            }
        }
        // метод - уведомить пользователей
        private void notifyUsers(Connection connection, String userName) throws IOException{
            Iterator<Map.Entry<String, Connection>> itr = connectionMap.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, Connection> entry = itr.next();
                Message message = new Message(MessageType.USER_ADDED, entry.getKey());
                if (!userName.equals(entry.getKey())) {
                    connection.send(message);
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    Message outputMessage = new Message(MessageType.TEXT, userName + ": " + message.getData());
                    sendBroadcastMessage(outputMessage);
                } else ConsoleHelper.writeMessage("FAIL!!!");
            }
        }

    }
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(ConsoleHelper.readInt())){
            ConsoleHelper.writeMessage("Сервер запущен...");
            while (true) {
                Handler handler = new Handler(server.accept());
                handler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
