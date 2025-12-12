package messages.response;

import messages.Message;
import messages.MsgHeader;

public class ChatReqDeniedMessage implements Message {
    private final MsgHeader header;
    private final String requested_user;

    public ChatReqDeniedMessage(MsgHeader header, String requested_user) {
        this.header = header;
        this.requested_user=requested_user;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    

    public String getRequested_user() {
        return requested_user;
    }

    @Override
    public String toString() {
        return String.format("CHAT_REQ_DENIED %S\r\n", requested_user);// according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount CRLF
    }

}
