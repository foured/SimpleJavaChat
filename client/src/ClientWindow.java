import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

public class ClientWindow extends JFrame implements TCPConnectionListener {
    private TCPConnection  connection;

    private static String IP_ADDR = "192.168.0.18";
    private static int PORT = 8189;
    public static final int WIDTH = 700;
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

    private String[] singleChats = {
        "Server chat"
    };
    private List<String> multiChats = new ArrayList<String>();

    private JPanel bottomPanel = new JPanel();
    private JPanel rightPanel = new JPanel();
    private JTextField fieldInput = new JTextField(50);
    private JTextArea chatInfo = new JTextArea(1, 30);
    private JTextArea log = new JTextArea(30, 15);
    private JButton addSingleChat = new JButton("Добавить пользователя");
    private JButton addMultiChat = new JButton("Создать чат");
    private JButton multiChatSettings = new JButton("Настроить чат");
    private JComboBox chats = new JComboBox(singleChats);

    private Map<String, String> chatsLogs = new HashMap<>(); //chat name, chat text
    private String currentChat;

    private ClientWindow(){
        //prepare
        nickname = JOptionPane.showInputDialog(this, "Введите nickname:");
        chatInfo.setText("Nickname: " + nickname + " | Chat with: " + chats.getSelectedItem());
        currentChat = singleChats[0];

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setEditable(false);
        chatInfo.setEditable(false);
        add(chatInfo, BorderLayout.NORTH);
        add(new JScrollPane(log), BorderLayout.CENTER);
        bottomPanel.add(fieldInput);
        bottomPanel.add(chats);
        add(bottomPanel, BorderLayout.SOUTH);

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        rightPanel.add(addSingleChat);
        rightPanel.add(addMultiChat);
        rightPanel.add(multiChatSettings);
        add(rightPanel, BorderLayout.EAST);

        setVisible(true);
        addActions();

        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
            System.out.println("connected");
        }catch (IOException | ClassNotFoundException e){
            printMsg("Connection exception: " + e, singleChats[0]);
        }
    }

    private void updateLogText(){
        currentChat = chats.getSelectedItem().toString();
        log.setText(chatsLogs.get(currentChat));
    }

    private void addActions(){
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

        addSingleChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = JOptionPane.showInputDialog(chatInfo.getParent(), "Введите nickname другого пользователя:");
                connection.sendMessage(new Message(MessageType.FIND_USER, userName));
            }
        });

        addMultiChat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String chatName = JOptionPane.showInputDialog(chatInfo.getParent(), "Введите название чата:");
                connection.sendMessage(new Message(MessageType.CREATE_CHAT, chatName));
            }
        });

        multiChatSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = JOptionPane.showInputDialog(chatInfo.getParent(), "Введите nickname другого пользователя:");
                String[] multiChats_arr = multiChats.toArray(new String[multiChats.size()]);
                String selectedChat = (String) JOptionPane.showInputDialog( chatInfo.getParent(), "Выберите чат",
                        "Чаты:",JOptionPane.QUESTION_MESSAGE,null, multiChats_arr, multiChats_arr[0] );

                connection.sendMessage(new Message(MessageType.ADD_USER_TO_CHAT, username, selectedChat));
            }
        });
    }

    @Override
    public void onConnectionRead(TCPConnection tcpConnection) {
        printMsg("Connection ready...", singleChats[0]);
    }

    @Override
    public void onReceiveMessage(TCPConnection tcpConnection, Message msg) {
        if(msg.getType() == MessageType.TEXT_FROM_USER) {
            printMsg(msg.getText(), msg.getChatName());
        }else if(msg.getType() == MessageType.TEXT_FROM_SERVER){
            printMsg(msg.getText(), singleChats[0]);
        } else if(msg.getType() == MessageType.REQUEST_USER_NAME){
            connection.sendMessage(new Message(MessageType.USER_NAME, nickname));
        }
        else if(msg.getType() == MessageType.USER_FOUND){
            chats.addItem(msg.getText());
            chatsLogs.put(msg.getText(), "Беседа создана" + "\n");
        }else if(msg.getType() == MessageType.ADDED_TO_CHAT){
            multiChats.add(msg.getText());
            chats.addItem(msg.getText());
            chatsLogs.put(msg.getText(), "Вас добавили в беседу" + "\n");
        }
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close", singleChats[0]);
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e, singleChats[0]);
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