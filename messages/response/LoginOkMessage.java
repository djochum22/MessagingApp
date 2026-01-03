package messages.response;

import messages.Message;
import messages.MsgHeader;

public class LoginOkMessage implements Message {
    private final MsgHeader header;

    public LoginOkMessage(MsgHeader header) {
        this.header=header;
    
    }

    @Override
    public MsgHeader header() {
        return header;
    }


    @Override
    public String toString() {
        return String.format("LOGIN_OK\r\n"); 
    }
    

}
