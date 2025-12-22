package messages.response;

import java.net.InetAddress;

import messages.Message;
import messages.MsgHeader;

public class SendPortMessage implements Message {
    private final MsgHeader header;
    private final int port;
    private final String publicKey;
    private final InetAddress ipAddress;

    public SendPortMessage(MsgHeader header, int port, String publicKey, InetAddress ipAddress) {
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

    

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return String.format("SEND_PORT %s %d %s\r\n",publicKey, port,ipAddress); 
    }

}
