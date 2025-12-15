package messages.response;

import messages.Message;
import messages.MsgHeader;

public class Quit implements Message {

    private final MsgHeader header;

    public Quit(MsgHeader header) {
        this.header = header;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("QUIT\r\n");
    }

}
