package messages.response;

import messages.Message;
import messages.MsgHeader;

public class ChatReqOkMessage implements Message {
    private final MsgHeader header;
    private final int requested_user_port;

    public ChatReqOkMessage(MsgHeader header, int requested_user_port) {
        this.header = header;
        this.requested_user_port =requested_user_port;
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }

    

    public int getRequested_user_port() {
        return requested_user_port;
    }

    @Override
    public String toString() {
        return String.format("CHAT_REQ_OK %d\r\n", requested_user_port); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount CRLF
    }

}
