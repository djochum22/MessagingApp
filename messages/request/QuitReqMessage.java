package messages.request;

import messages.Message;
import messages.MsgHeader;

public class QuitReqMessage implements Message {

    private final MsgHeader header;

    public QuitReqMessage(MsgHeader header) {
        this.header = header;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("QUIT_REQ\r\n"); // according to ABNF abheben_req = "QUIT_REQ" CRLF
    }

}