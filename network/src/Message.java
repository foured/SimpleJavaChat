import java.io.Serializable;

public class Message implements Serializable {
    private MessageType type;
    private String text;

    public Message(MessageType messageType){
        type = messageType;
        text = null;
    }

    public Message(String value){
        type = MessageType.TEXT;
        text = value;
    }

    public  Message(MessageType messageType, String value){
        type = messageType;
        text = value;
    }

    public String getText() {return text;}

    public MessageType getType() {return type;}
}
