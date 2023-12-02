import java.awt.*;
import java.io.Serializable;

public class Message implements Serializable {
    private MessageType type;
    private String text;
    private String chatName;

    public Message(MessageType messageType){
        type = messageType;
        text = null;
        chatName = null;
    }

    public Message(String value, String chatName){
        type = MessageType.TEXT_FROM_USER;
        text = value;
        this.chatName = chatName;
    }

    public  Message(MessageType messageType, String value){
        type = messageType;
        text = value;
        chatName = null;
    }

    public String getText() {return text;}
    public MessageType getType() {return type;}
    public String getChatName() {return chatName;}
}
