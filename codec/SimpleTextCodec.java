package codec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import messages.Message;
import messages.MsgHeader;
import messages.MsgType;
import messages.request.ChatReqMessage;
import messages.request.LoginMessage;
import messages.request.LogoutMessage;
import messages.request.QuitReqMessage;
import messages.request.RegisterMessage;
import messages.request.WhoOnlineMessage;
import messages.response.ChatReqDeniedMessage;
import messages.response.ChatReqOkMessage;
import messages.response.ForwardChatRequestMessage;
import messages.response.OkMessage;
import messages.response.SendPortMessage;
import messages.UDP_messages.ChatMessage;

public final class SimpleTextCodec {

    public byte[] encode(Message msg) {
        String msgBody;
        String msgHeader = msg.header().toString();

        msgBody = msg.toString();
        return (msgHeader + "\r\n\r\n" + msgBody).getBytes(StandardCharsets.UTF_8);
    }

    public Message decode(byte[] sb) {

        String s = new String(sb, StandardCharsets.UTF_8);
        MsgHeader header;
        Message msg = null;
        String headerLines[], bodyLines[];

        String[] parts = s.split("\r\n\r\n"); // should be 2 (header SP body); split at \r\n\r\n
        headerLines = parts[0].split("\r\n"); // splits lines of header
        bodyLines = parts[1].split("\r\n"); // splits lines of body

        try {
            header = constructHeader(headerLines);
            msg = constructMessage(bodyLines, header);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return msg;
    }

    public MsgHeader constructHeader(String headerLines[]) throws Exception {

    
        MsgType type = null;
        int msgId = 0;
        int correlationId = 0;
        long timestampMillis = 0;
        MsgHeader header = null;

        for (String part : headerLines) {
            String[] headerFields = part.split(": ", 2); // from "Version: 1" to "Version" and "1" to isolate value
            if (headerFields.length != 2)
                continue;
            switch (headerFields[0]) {
                case "Type":
                    type = MsgType.valueOf(headerFields[1].toUpperCase());
                    break;
                case "MsgId":
                    msgId = Integer.parseInt(headerFields[1]);
                    break;
                case "CorrelationId":
                    correlationId = Integer.parseInt(headerFields[1]);
                    break;
                case "Timestamp in Millis":
                    timestampMillis = System.currentTimeMillis();
                    break;
                default:
                    throw new Exception("Unsupported Header-Field");
            }

        }
        header = new MsgHeader(type, msgId, correlationId, timestampMillis);
        return header;

    }

    public Message constructMessage(String bodyLines[], MsgHeader header) throws Exception {

        Message msg = null;
        String email;
        String username;
        String password;
        String requested_user = null;
        int requested_user_port;
        int port;
        String text;

        for (String part : bodyLines) {

            String command = part.split(" ")[0];
            String bodyFields[] = part.split(" ");
            bodyFields = Arrays.copyOfRange(bodyFields, 1, bodyFields.length); // remove first word

            switch (command) {
                case "REGISTER":
                    email = bodyFields[0];
                    username = bodyFields[1];
                    password = bodyFields[2];
                    msg = new RegisterMessage(header, email, username, password);
                    break;
                case "LOGIN":
                    email = bodyFields[0];
                    password = bodyFields[1];
                    msg = new LoginMessage(header, email, password);
                    break;
                case "CHAT_REQ":
                    requested_user = bodyFields[0];
                    msg = new ChatReqMessage(header, requested_user);
                    break;
                case "WHO_ONLINE":
                    msg = new WhoOnlineMessage(header);
                    break;
                case "LOGOUT":
                    msg = new LogoutMessage(header);
                    break;
                case "QUITREQ":
                    msg = new QuitReqMessage(header);
                    break;
                case "OK":
                    msg = new OkMessage(header);
                    break;
                case "CHAT_REQ_OK":
                    requested_user_port = Integer.parseInt(bodyFields[0]);
                    msg = new ChatReqOkMessage(header, requested_user_port);
                    break;
                case "USERS_ONLINE":
                    // TODO not certain about the format for the 
                    // split the toString of the list and turn into a new arraylist
                case "SEND_PORT":
                    port = Integer.parseInt(bodyFields[0]);
                    username = bodyFields[1];
                    msg = new SendPortMessage(header, port, username);
                    break;
                case "CHAT_REQ_DENIED":
                    requested_user = bodyFields[0];
                    msg = new ChatReqDeniedMessage(header, requested_user);
                    break;
                case "ERROR":
                    // TODO should I read out of the file the error messages?
                case "FWD_CHAT_REQ":
                    requested_user = bodyFields[0];
                    msg = new ForwardChatRequestMessage(header, requested_user);
                    break;
                case "CHAT_MSG":
                    text = bodyFields[0];
                    msg = new ChatMessage(header, text);
                    break;
                default:
                    throw new Exception("Unsupported Body-Field");
            }

        }

        return msg;

    }
}
