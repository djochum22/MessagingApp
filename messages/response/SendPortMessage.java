package messages.response;

import java.net.InetAddress;
import java.security.PublicKey;
import java.util.Base64;

import messages.Message;
import messages.MsgHeader;

public class SendPortMessage implements Message {
    private final MsgHeader header;
    private final int port;
    private final PublicKey publicKey;
    private final String ipAddress;

    public SendPortMessage(MsgHeader header, int port, PublicKey publicKey, String ipAddress) {
        this.header = header;
        this.port = port;
        this.publicKey=publicKey;
        this.ipAddress=ipAddress;
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }

    public int getPort() {
        return port;
    }

    

    public String getIpAddress() {
        return ipAddress;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return String.format("SEND_PORT %d %s %s\r\n", port, Base64.getEncoder().encodeToString(publicKey.getEncoded()) ,ipAddress); 
    }

}
