package user;

import java.io.DataOutputStream;
import java.net.Socket;
import java.security.PublicKey;

public class User {
    private String email;
    private String name;
    private String hashedPassword;
    private String ip;
    private int udpPort;
    private Socket tcpSocket;
    private PublicKey publicKey;
    private String saltEncoded;
    private int iterations;

    private DataOutputStream out = null;

    public User(String email, String name, String hashedPassword, String saltEncoded, int iterations, String ip, Socket tcpSocket, DataOutputStream out){
        this.email = email;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.saltEncoded=saltEncoded;
        this.iterations =iterations;
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

    public String gethashedPassword() {
        return hashedPassword;
    }

    public String getSaltEncoded() {
        return saltEncoded;
    }

    public int getIterations() {
        return iterations;
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

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Socket getTcpSocket() {
        return tcpSocket;
    }


    public DataOutputStream getOut() {
        return out;
    }

}
