package messages.request;

import messages.Message;
import messages.MsgHeader;

public class KarteMessage implements Message {

    private final MsgHeader header;
    private final String card;

    public KarteMessage(MsgHeader header, String card) {
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
        return String.format("KARTE %s\r\n", card); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount
                                                    // CRLF
    }

}
