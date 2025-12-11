package messages.response;

import messages.Message;
import messages.MsgHeader;

public class OkMessage implements Message {
    private final MsgHeader header;

    public OkMessage(MsgHeader header) {
        this.header=header;
    
    }

    @Override
    public MsgHeader header() {
        return header;
    }


    @Override
    public String toString() {
        return String.format("OK\r\n"); 
    }
    

}
