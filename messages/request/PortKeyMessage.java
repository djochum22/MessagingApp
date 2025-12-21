package messages.request;

import messages.Message;
import messages.MsgHeader;

public class PortKeyMessage  implements Message{

    private final MsgHeader header;
    private String publicKey;
    private int udpPort;


    public PortKeyMessage(MsgHeader header, String publicKey, int udpPort) {
        this.header = header;
        this.publicKey = publicKey;
        this.udpPort = udpPort;
    }

    @Override
    public MsgHeader header() {
        return header;
    }


    public String getPublicKey() {
        return publicKey;
    }

    public int getGetUdpPort() {
        return udpPort;
    }

    @Override
    public String toString() {
        return String.format("SEND_PORT_KEY %s %d\r\n", publicKey, udpPort);
    }


}

    

