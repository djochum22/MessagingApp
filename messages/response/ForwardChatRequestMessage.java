package messages.response;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Base64;

import messages.Message;
import messages.MsgHeader;

public class ForwardChatRequestMessage implements Message {

    private final MsgHeader header;
   
    private  int udpPort=0;
    private  String ip=null;
    private  String requesting_user=null;
    private PublicKey publicKey=null;

    public ForwardChatRequestMessage(MsgHeader header, String requesting_user,String ip,  int udpPort,PublicKey publicKey) {
        this.header = header;
        this.requesting_user = requesting_user;
        this.ip=ip;
        this.udpPort =udpPort;
        this.publicKey=publicKey;
    }
 
    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("FWD_CHAT_REQ %s %s %d %s\r\n", requesting_user, ip, udpPort,Base64.getEncoder().encodeToString(publicKey.getEncoded()));
    }

    

    public int getUdpPort() {
        return udpPort;
    }

    public String getIp() {
        return ip;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    

    public String getRequesting_user() {
        return requesting_user;
    }

}
