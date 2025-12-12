package user;

import java.net.InetAddress;

public class User {
    private String email;
    private String name;
    private String password;
    private  InetAddress ip;
    private  int udpPort;
    

    public User(String email, String name, String password, InetAddress ip, int udpPort) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.ip = ip;
        this.udpPort = udpPort;
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

    public InetAddress getIp() {
        return ip;
    }

    public int getUdpPort() {
        return udpPort;
    }

 
}
