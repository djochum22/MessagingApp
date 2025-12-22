package messages.response;

import messages.Message;
import messages.MsgHeader;

public class RegistrationOkMessage implements Message {
    private final MsgHeader header;

    public RegistrationOkMessage(MsgHeader header) {
        this.header=header;
    
    }

    @Override
    public MsgHeader header() {
        return header;
    }


    @Override
    public String toString() {
        return String.format("REG_OK\r\n"); 
    }
    

}
