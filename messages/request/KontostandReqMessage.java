package messages.request;

import messages.Message;
import messages.MsgHeader;

public class KontostandReqMessage implements Message {

    private final MsgHeader header;
    private final String card;

    public KontostandReqMessage(MsgHeader header, String card) {
        this.header = header;
        this.card = card;

    }

    @Override
    public MsgHeader header() {
        return header;
    }

    public String card() {
        return card;
    }

    @Override
    public String toString() {
        return String.format("KONTOSTAND_REQ  %s\r\n", card); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card
                                                              // SP amount CRLF
    }

}
