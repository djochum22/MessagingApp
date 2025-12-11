package messages.response;

import messages.Message;
import messages.MsgHeader;

public class KontostandMessage implements Message {
    private final MsgHeader header;
    private final double amount;

    public KontostandMessage(MsgHeader header, double amount) {
        this.header = header;
        this.amount = amount;
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }

    public double amount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("KONTOSTAND %.2f\r\n", amount); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP
                                                             // amount CRLF
    }

}
