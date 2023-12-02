import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxTread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException{
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.socket = socket;
        this.eventListener = eventListener;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        rxTread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionRead(TCPConnection.this);
                    while(!rxTread.isInterrupted()) {
                        String msg = in.readLine();
                        eventListener.onReceiveString(TCPConnection.this, msg);
                    }
                }catch(IOException e){
                    eventListener.onException(TCPConnection.this, e);
                }
            }
        });
        rxTread.start();
    }

    public synchronized void sendString(String value){
        try{
            out.write(value + "\r\n");
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
