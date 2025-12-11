package messages.response;

import messages.Message;
import messages.MsgHeader;

public class PinOkMessage implements Message {
    private final MsgHeader header;

    public PinOkMessage(MsgHeader header) {
        this.header = header;
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("PIN_OK\r\n"); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount CRLF
    }

}
