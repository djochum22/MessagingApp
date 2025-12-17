package messages.response;

import messages.Message;
import messages.MsgHeader;

public class ChatReqOkMessage implements Message {
    private final MsgHeader header;
    private final int requested_user_port;
    private final String reqAddress;
    private final int personal_port;

    public ChatReqOkMessage(MsgHeader header, int requested_user_port, String reqAddress, int personal_port) {
        this.header = header;
        this.requested_user_port =requested_user_port;
        this.reqAddress = reqAddress;
        this.personal_port = personal_port;
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
        return String.format("CHAT_REQ_OK %d %s %d\r\n", requested_user_port, reqAddress, personal_port); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount CRLF
    }

    public String getRequested_user_ipAddress() {
        return reqAddress;
    }

    public int getPersonal_port() {
        return personal_port;
    }

}
