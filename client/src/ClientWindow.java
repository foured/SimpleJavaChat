import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    private String[] items = {
        "Server chat"
    };
    private JPanel bottomPanel = new JPanel();
    private JTextField fieldInput = new JTextField(40);
    private JTextArea chatInfo = new JTextArea(1, 30);
    private JTextArea log = new JTextArea(30, 15);
    private JButton addChat = new JButton("Добавить чат");
    private JComboBox chats = new JComboBox(items);

    private Map<String, String> chatsLogs = new HashMap<>();
    private String currentChat;

    private ClientWindow(){
        //prepare
        nickname = JOptionPane.showInputDialog(this, "Введите nickname:");
        chatInfo.setText("Nickname: " + nickname + " | Chat with: " + chats.getSelectedItem());
        currentChat = items[0];

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setEditable(false);
        chatInfo.setEditable(false);
        add(chatInfo, BorderLayout.NORTH);
        add(new JScrollPane(log), BorderLayout.CENTER);
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
                connection.sendMessage(new Message(MessageType.TEXT_FROM_USER, nickname + ": " + msg, chats.getSelectedItem().toString()));
            }
        });

        chats.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatInfo.setText("Nickname: " + nickname + " | Chat with: " + chats.getSelectedItem());
                updateLogText();
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
            printMsg("Connection exception: " + e, items[0]);
        }
    }

    private void updateLogText(){
        currentChat = chats.getSelectedItem().toString();
        log.setText(chatsLogs.get(currentChat));
    }

    @Override
    public void onConnectionRead(TCPConnection tcpConnection) {
        printMsg("Connection ready...", items[0]);
    }

    @Override
    public void onReceiveMessage(TCPConnection tcpConnection, Message msg) {
        if(msg.getType() == MessageType.TEXT_FROM_USER) {
            printMsg(msg.getText(), msg.getChatName());
        }else if(msg.getType() == MessageType.TEXT_FROM_SERVER){
            printMsg(msg.getText(), items[0]);
        } else if(msg.getType() == MessageType.REQUEST_USER_NAME){
            connection.sendMessage(new Message(MessageType.USER_NAME, nickname));
        }
        else if(msg.getType() == MessageType.USER_FOUND){
            chats.addItem(msg.getText());
            chatsLogs.put(msg.getText(), "Беседа создана" + "\n");
        }
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close", items[0]);
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e, items[0]);
    }

    private synchronized void printMsg(String msg, String chatName){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatsLogs.put(chatName,
                        chatsLogs.get(chatName) == null ? (msg + "\n") :  chatsLogs.get(chatName) + (msg + "\n"));
                updateLogText();
            }
        });
    }
}