package messages.request;

import messages.Message;
import messages.MsgHeader;

public class WhoOnlineMessage implements Message {
    private final MsgHeader header;

    public WhoOnlineMessage(MsgHeader header) {
        this.header = header;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("WHO_ONLINE\r\n");
    }

}
