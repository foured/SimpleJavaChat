import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class ChatServer implements TCPConnectionListener{
    private static final int PORT = 8189;
    public static void main(String[] args) {
        //Scanner scanner = new Scanner(System.in);
        //System.out.println("Enter port: ");
        //PORT = scanner.nextInt();

        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private final Map<TCPConnection, String> connectionsNames = new HashMap<>();

    private ChatServer(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            while(true){
                try {
                    new TCPConnection(this, serverSocket.accept());
                    System.out.println(serverSocket.getLocalSocketAddress());
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
        if(msg.getType() == MessageType.TEXT){ sendToAllConnections(msg.getText()); }
        else if(msg.getType() == MessageType.USER_NAME){
            connections.add(tcpConnection);
            connectionsNames.put(tcpConnection, msg.getText());
            sendToAllConnections("Client connected: " + msg.getText());
        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        sendToAllConnections("Client disconnected: " + connectionsNames.remove(tcpConnection));
        connections.remove(tcpConnection);
        connectionsNames.remove(tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value){
        System.out.println(value);
        for (TCPConnection connection : connections) {
            connection.sendMessage(new Message(value));
        }
    }
}