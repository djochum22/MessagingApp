package messages.request;

import messages.Message;
import messages.MsgHeader;

public class LoginMessage implements Message {

    private final MsgHeader header;
    private String email, password;

    public LoginMessage(MsgHeader header, String email, String password) {
        this.header = header;
        this.email = email;
        this.password = password;
    }

    @Override
    public MsgHeader header() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("LOGIN %s %s\r\n", email, password); // according to ABNF abheben_req = "QUIT_REQ" CRLF
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}