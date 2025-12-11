package messages.request;

import messages.Message;
import messages.MsgHeader;

public class AbhebenReqMessage implements Message {

    private final MsgHeader header;
    private final String card;
    private final double amount;

    public AbhebenReqMessage(MsgHeader header, String card, double amount) {
        this.header = header;
        this.card = card;
        this.amount = amount;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    public String card() {
        return card;
    }

    public double amount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("ABHEBEN_REQ %s %.2f\r\n", card, amount);// according to ABNF abheben_req = "ABHEBEN_REQ"
                                                                      // SP card SP amount CRLF
    }

}
