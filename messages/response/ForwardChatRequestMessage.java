package messages.response;

import messages.Message;
import messages.MsgHeader;

public class ForwardChatRequestMessage implements Message {

    private final MsgHeader header;
    private String requesting_user;

    public ForwardChatRequestMessage(MsgHeader header, String requesting_user) {
        this.header = header;
        this.requesting_user = requesting_user;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("FWD_CHAT_REQ  %s\r\n", requesting_user);
    }

    public String getRequesting_user() {
        return requesting_user;
    }

}
