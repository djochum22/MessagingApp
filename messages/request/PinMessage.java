package messages.request;

import messages.Message;
import messages.MsgHeader;

public class PinMessage implements Message {

    private final MsgHeader header;
    private final int pin;

    public PinMessage(MsgHeader header, int pin) {
        this.header = header;
        this.pin = pin;

    }

    @Override
    public MsgHeader header() {
        return header;
    }

    public int pinLength() {
        return String.valueOf(Math.abs(pin)).length();
    }
    
    public int pin() {
        return pin;
    }

    @Override
    public String toString() {
        return String.format("PIN %d\r\n", pin); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount CRLF
    }

}
