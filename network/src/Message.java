import java.awt.*;
import java.io.Serializable;

public class Message implements Serializable {
    private MessageType type;
    private String text;
    private String chatName;

    public Message(MessageType messageType){
        /*
            Message types:
                * REQUEST_USER_NAME
         */

        type = messageType;
        text = null;
        chatName = null;
    }

    public Message(MessageType messageType, String value, String chatName){
        /*
            Message types:
                * TEXT_FROM_USER, text, recipient username / chat name
                * ADD_USER_TO_CHAT, username, chat name
         */

        type = messageType;
        text = value;
        this.chatName = chatName;
    }

    public  Message(MessageType messageType, String value){
        /*
            Message types:
                * FIND_USER, username
                * USER_FOUND, username
                * USER_NAME, username
                * TEXT_FROM_SERVER, text
                * CREATE_CHAT, chat name
                * ADDED_TO_CHAT, chat name
         */
        type = messageType;
        text = value;
        chatName = null;
    }

    public String getText() {return text;}
    public MessageType getType() {return type;}
    public String getChatName() {return chatName;}
}
