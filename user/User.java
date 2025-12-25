package user;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class User {
    private String email;
    private String name;
    private String password;
    private String ip;
    private int udpPort;
    private Socket tcpSocket;
    private String publicKey;
    private DataOutputStream out = null;

    public User(String email, String name, String password, String ip, Socket tcpSocket, DataOutputStream out){
        this.email = email;
        this.name = name;
        this.password = password;
        this.ip = ip;
        this.tcpSocket =tcpSocket;
        this.udpPort=0;
        this.publicKey=null;
        this.out=out;
        
    }

    

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getIp() {
        return ip;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Socket getTcpSocket() {
        return tcpSocket;
    }


    public DataOutputStream getOut() {
        return out;
    }

}
