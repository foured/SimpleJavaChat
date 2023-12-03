import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class ChatServer implements TCPConnectionListener{
    private static final int PORT = 8189;
    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private final Map<String, TCPConnection> connectionsMap = new HashMap<>();
    private final Map<String, List<String>> chatsMap = new HashMap<>();

    private ChatServer(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            while(true){
                try {
                    new TCPConnection(this, serverSocket.accept());
                    System.out.println("Connected: " + serverSocket.getLocalSocketAddress());
                }catch (IOException | ClassNotFoundException e){
                    System.out.println("TCPConnection: " + e);
                }
            }
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionRead(TCPConnection tcpConnection) {
        tcpConnection.sendMessage(new Message(MessageType.REQUEST_USER_NAME));
    }

    @Override
    public synchronized void onReceiveMessage(TCPConnection tcpConnection, Message msg) {
        System.out.println(tcpConnection.toString() + " " + msg.getType());
        if(msg.getType() == MessageType.TEXT_FROM_USER){
            if(connectionsMap.containsKey(msg.getChatName())) {
                tcpConnection.sendMessage(new Message(MessageType.TEXT_FROM_USER, msg.getText(), msg.getChatName()));
                connectionsMap.get(msg.getChatName())
                        .sendMessage(new Message(MessageType.TEXT_FROM_USER, msg.getText(),
                                reverceConnectionsMap().get(tcpConnection)));
            }
            else if(chatsMap.containsKey(msg.getChatName())){
                sendToAllChatUsers(msg.getText(), msg.getChatName());
            }
        }
        else if(msg.getType() == MessageType.USER_NAME){
            connections.add(tcpConnection);
            connectionsMap.put(msg.getText(), tcpConnection);
            System.out.println("Пользователь добавлен: '" + msg.getText() + "'");
        }
        else if(msg.getType() == MessageType.FIND_USER){
            if(connectionsMap.containsKey(msg.getText())) {
                tcpConnection.sendMessage(new Message(MessageType.TEXT_FROM_SERVER, "Пользователь найден"));
                tcpConnection.sendMessage(new Message(MessageType.USER_FOUND, msg.getText()));

                connectionsMap.get(msg.getText()).sendMessage(
                        new Message(MessageType.TEXT_FROM_SERVER, "Пользователь найден"));
                connectionsMap.get(msg.getText()).sendMessage(
                        new Message(MessageType.USER_FOUND, reverceConnectionsMap().get(tcpConnection)));
            }
            else {
                tcpConnection.sendMessage(new Message(MessageType.TEXT_FROM_SERVER, "Пользователь c именем '"
                        + msg.getText() + "' не найден"));
            }
        }else if(msg.getType() == MessageType.CREATE_CHAT){
            List<String> names = new ArrayList<>();
            String sender = reverceConnectionsMap().get(tcpConnection);
            names.add(sender);
            chatsMap.put(msg.getText(), names);
            tcpConnection.sendMessage(new Message(MessageType.TEXT_FROM_SERVER, "Чат создан"));
            tcpConnection.sendMessage(new Message(MessageType.ADDED_TO_CHAT, msg.getText()));
        }else if(msg.getType() == MessageType.ADD_USER_TO_CHAT){
            if(!connectionsMap.containsKey(msg.getText()))
                tcpConnection.sendMessage(new Message(MessageType.TEXT_FROM_SERVER, "Пользователь c именем '"
                        + msg.getText() + "' не найден"));

            List<String> names = chatsMap.get(msg.getChatName());
            names.add(msg.getText());
            chatsMap.put(msg.getChatName(), names);
            sendToAllChatUsers("Пользователь '" + msg.getText() + "' добавлен", msg.getChatName());
            TCPConnection target = connectionsMap.get(msg.getText());
            target.sendMessage(new Message(MessageType.ADDED_TO_CHAT, msg.getChatName()));
            target.sendMessage(new Message(MessageType.TEXT_FROM_SERVER,
                    "Вас добавили в беседу '" + msg.getChatName() + "'"));
        }
    }

    private void sendToAllChatUsers(String text, String chatName){
        List<String> names = chatsMap.get(chatName);
        for(String name : names){
            connectionsMap.get(name).sendMessage(new Message(MessageType.TEXT_FROM_USER, text, chatName));
        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        connectionsMap.entrySet()
                .removeIf(entry -> entry.getValue() == tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private Map<TCPConnection, String> reverceConnectionsMap(){
        Map<TCPConnection, String> revConnectionsMap = new HashMap<>();
        for(Map.Entry<String, TCPConnection> entry : connectionsMap.entrySet()){
            revConnectionsMap.put(entry.getValue(), entry.getKey());
        }
        return revConnectionsMap;
    }
}