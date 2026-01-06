package messages.request;

import java.security.PublicKey;
import java.util.Base64;

import messages.Message;
import messages.MsgHeader;

public class PortKeyMessage  implements Message{

    private final MsgHeader header;
    private PublicKey publicKey;
    private int udpPort;


    public PortKeyMessage(MsgHeader header, PublicKey publicKey, int udpPort) {
        this.header = header;
        this.publicKey = publicKey;
        this.udpPort = udpPort;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public int getGetUdpPort() {
        return udpPort;
    }

    // Converting the PublicKey object into a byte[] then encoding that into a string
    @Override
    public String toString() {
        return String.format("PORT_KEY %s %d\r\n", Base64.getEncoder().encodeToString(publicKey.getEncoded()), udpPort);
    }


}

    

