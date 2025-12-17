package messages.response;

import java.net.InetAddress;

import messages.Message;
import messages.MsgHeader;

public class ChatReqOkMessage implements Message {
    private final MsgHeader header;
    private final int requested_user_port;
    private final String reqAddress;

    public ChatReqOkMessage(MsgHeader header, int requested_user_port, String reqAddress) {
        this.header = header;
        this.requested_user_port =requested_user_port;
        this.reqAddress = reqAddress;
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
        return String.format("CHAT_REQ_OK %d %s\r\n", requested_user_port, reqAddress); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount CRLF
    }

    public String getRequested_user_ipAddress() {
        return reqAddress;
    }

}
