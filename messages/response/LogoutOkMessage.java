package messages.response;

import messages.Message;
import messages.MsgHeader;

public class LogoutOkMessage implements Message {
    private final MsgHeader header;

    public LogoutOkMessage(MsgHeader header) {
        this.header=header;
    
    }

    @Override
    public MsgHeader header() {
        return header;
    }


    @Override
    public String toString() {
        return String.format("LOGOUT_OK\r\n"); 
    }
    

}
