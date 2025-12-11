package messages.response;

import messages.Message;
import messages.MsgHeader;

public class SendPortMessage implements Message {
    private final MsgHeader header;
    private final int port;
    private final String username;

    public SendPortMessage(MsgHeader header, int port, String username) {
        this.header = header;
        this.port = port;
        this.username=username;
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }

    public int getPort() {
        return port;
    }

    

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return String.format("SEND_PORT %s %d\r\n",username, port); 
    }

}
