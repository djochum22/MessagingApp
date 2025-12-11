package messages.request;

import messages.Message;
import messages.MsgHeader;

public class ByeMessage implements Message {

    private final MsgHeader header;
    private final String text;

    public ByeMessage(MsgHeader header, String text) {
        this.header = header;
        this.text = text;
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return String.format("BYE %s\r\n", text); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount
                                                  // CRLF
    }

}
