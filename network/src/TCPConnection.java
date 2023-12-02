import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxTread;
    private final TCPConnectionListener eventListener;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException, ClassNotFoundException{
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException, ClassNotFoundException {
        this.socket = socket;
        this.eventListener = eventListener;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        rxTread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionRead(TCPConnection.this);
                    while(!rxTread.isInterrupted()) {
                        Message msg = (Message) in.readObject();
                        eventListener.onReceiveMessage(TCPConnection.this, msg);
                    }
                }catch(IOException | ClassNotFoundException e){
                    eventListener.onException(TCPConnection.this, e);
                }finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxTread.start();
    }

    public synchronized void sendMessage(Message value){
        try{
            out.writeObject(value);
            out.flush();
        } catch(IOException e){
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        rxTread.interrupt();
        try{
            socket.close();
        } catch(IOException e){
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString(){
        return "TCPListener: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
