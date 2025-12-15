package messages.UDP_messages;

public class ChatMessageACK  {
   private final int ack;

    public ChatMessageACK(int ack) {
        this.ack = ack;
    }


    public int ack() {
        return ack;
    }


    public byte[] data() {
        return (new String(String.valueOf(ack))).getBytes();

}

}