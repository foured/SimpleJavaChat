import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static String IP_ADDR = "192.168.0.18";
    private static int PORT = 8189;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickname = new JTextField("foured");
    private final JTextField fieldInput = new JTextField();

    private TCPConnection  connection;

    private ClientWindow(){
        //IP_ADDR = JOptionPane.showInputDialog(this, "Enter server ip");
        //PORT = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter port"));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setForeground(Color.RED);
        log.setBackground(Color.BLACK);
        log.setEnabled(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);

        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
            System.out.println("connected");
        }catch (IOException | ClassNotFoundException e){
            printMsg("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if(msg.isEmpty()) return;
        fieldInput.setText(null);
        connection.sendMessage(new Message(fieldNickname.getText() + ": " + msg));
    }

    @Override
    public void onConnectionRead(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void onReceiveMessage(TCPConnection tcpConnection, Message msg) {
        if(msg.getType() == MessageType.TEXT) printMsg(msg.getText());
        else if(msg.getType() == MessageType.REQUEST_USER_NAME){
            connection.sendMessage(new Message(MessageType.USER_NAME, fieldNickname.getText()));
        }
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    private synchronized void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}