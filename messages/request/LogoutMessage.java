package messages.request;

import messages.Message;
import messages.MsgHeader;

public class LogoutMessage implements Message {

    private final MsgHeader header;

    public LogoutMessage(MsgHeader header) {
        this.header = header;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("LOGOUT\r\n");
    }

}
