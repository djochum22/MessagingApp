package messages.request;

import messages.Message;
import messages.MsgHeader;

public class ChatReqMessage implements Message {

    private final MsgHeader header;
    private String requested_user;
   // private int requesting_user_port;

    public ChatReqMessage(MsgHeader header, String requested_user /*int requesting_user_port*/ ) {
        this.header = header;
        this.requested_user = requested_user;
       // this.requesting_user_port= requesting_user_port;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("CHAT_REQ %s\r\n", requested_user /*, requesting_user_port*/);
    }

    public String getRequested_user() {
        return requested_user;
    }

    // public int getRequesting_user_port() {
    //     return requesting_user_port;
    // }

}
