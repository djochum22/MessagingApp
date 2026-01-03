package messages.response;

import messages.Message;
import messages.MsgHeader;

public class ChatReqOkMessage implements Message {
    private final MsgHeader header;
    private final String reqUserName;
   
    public ChatReqOkMessage(MsgHeader header,String reqUserName) {
        this.header = header;
       this.reqUserName = reqUserName;
       
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }


    @Override
    public String toString() {
        return String.format("CHAT_REQ_OK %s\r\n", reqUserName); // 
    }

    public String getReqUserName() {
        return reqUserName;
    }

}
