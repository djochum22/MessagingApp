package messages.request;

import messages.Message;
import messages.MsgHeader;

public class RegisterMessage implements Message{
    private final MsgHeader header;
    private String email, username, password;

    public RegisterMessage(MsgHeader header, String email, String username, String password) {
        this.header = header;
        this.email = email;
        this.username = username;
        this.password = password;
    }
    
    @Override
    public MsgHeader header() {
       return header;
    }

    @Override
    public String toString() {
        return String.format("REGISTER %s %s %s\r\n", email, username, password); // according to ABNF abheben_req = "ABHEBEN_REQ" SP card SP amount CRLF
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
}
