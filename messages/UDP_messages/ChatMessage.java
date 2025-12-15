package messages.UDP_messages;

import messages.Message;
import messages.MsgHeader;

public class ChatMessage implements Message {
    private final MsgHeader header;
    private final String text;

    public ChatMessage(MsgHeader header, String text) {
        this.header = header;
        this.text = text;
    }
    @Override
    public MsgHeader header() {
        return header;
    }

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return String.format("CHAT_MSG %s\r\n", text); 

}

}
