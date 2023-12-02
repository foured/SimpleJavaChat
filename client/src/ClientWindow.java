import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class ClientWindow extends JFrame implements TCPConnectionListener {
    private TCPConnection  connection;

    private static String IP_ADDR = "192.168.0.18";
    private static int PORT = 8189;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 590;
    public final String nickname;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private JPanel bottomPanel = new JPanel();
    private JTextField fieldInput = new JTextField(40);
    private JTextArea chatInfo = new JTextArea(1, 30);
    private JTextArea messages = new JTextArea(30, 15);
    private JButton addChat = new JButton("Добавить чат");

    private String[] items = {
        "Server chat"
    };
    private JComboBox chats = new JComboBox(items);

    private ClientWindow(){
        nickname = JOptionPane.showInputDialog(this, "Введите nickname:");
        chatInfo.setText("Nickname: " + nickname + " | Chat with: " + chats.getSelectedItem());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        chatInfo.setEditable(false);
        messages.setEditable(false);
        add(new JScrollPane(chatInfo), BorderLayout.NORTH);
        add(new JScrollPane(messages), BorderLayout.CENTER);
        bottomPanel.add(fieldInput);
        bottomPanel.add(addChat);
        bottomPanel.add(chats);
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);

        fieldInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = fieldInput.getText();
                if(msg.isEmpty()) return;
                fieldInput.setText(null);
                connection.sendMessage(new Message(nickname + ": " + msg, chats.getSelectedItem().toString()));
            }
        });

        chats.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatInfo.setText("Nickname: " + nickname + " | Chat with: " + chats.getSelectedItem());
            }
        });

        addChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = JOptionPane.showInputDialog(chatInfo.getParent(), "Введите nickname другого пользователя:");
                connection.sendMessage(new Message(MessageType.FIND_USER, userName));
            }
        });

        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
            System.out.println("connected");
        }catch (IOException | ClassNotFoundException e){
            printMsg("Connection exception: " + e);
        }
    }

    @Override
    public void onConnectionRead(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void onReceiveMessage(TCPConnection tcpConnection, Message msg) {
        if(msg.getType() == MessageType.TEXT_FROM_USER || msg.getType() == MessageType.TEXT_FROM_SERVER) {
            printMsg(msg.getText());
        }
        else if(msg.getType() == MessageType.REQUEST_USER_NAME){
            connection.sendMessage(new Message(MessageType.USER_NAME, nickname));
        }
        else if(msg.getType() == MessageType.USER_FOUND){
            chats.addItem(msg.getText());
            chats.setSelectedItem(msg.getText());
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
                messages.append(msg + "\n");
                messages.setCaretPosition(messages.getDocument().getLength());
            }
        });
    }
}