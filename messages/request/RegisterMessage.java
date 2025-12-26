package messages.request;

import java.nio.charset.StandardCharsets;

import messages.Message;
import messages.MsgHeader;

public class RegisterMessage implements Message {
    private final MsgHeader header;
    private String email, username, hashedPassword;
    private  String saltEncoded;
    private int iterations;

    public RegisterMessage(MsgHeader header, String email, String username, String hashedPassword,   String saltEncoded ,
            int iterations) {
        this.header = header;
        this.email = email;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.saltEncoded  =  saltEncoded ;
        this.iterations = iterations;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getSaltEncoded() {
        return saltEncoded;
    }

    public int getIterations() {
        return iterations;
    }

    @Override
    public String toString() {
        return String.format("REGISTER %s %s %s %s %d\r\n", email, username, hashedPassword,saltEncoded,iterations);
               
    }

}
