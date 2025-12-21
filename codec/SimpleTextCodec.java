package codec;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import messages.response.ErrorMessage;
import messages.response.ForwardChatRequestMessage;
import messages.response.OkMessage;
import messages.response.SendPortMessage;
import messages.response.UsersOnlineMessage;
import messages.UDPmessages.ChatMessage;

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
        String reqAddress = null;
        int personal_port=0;
        String publicKey=null;
        InetAddress ipAddress=null;

        int port;
        String text = null;
        int errorCode;
        ArrayList<String> onlineUsers = new ArrayList<>();

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
                case "QUIT_REQ":
                    msg = new QuitReqMessage(header);
                    break;
                case "OK":
                    msg = new OkMessage(header);
                    break;
                case "CHAT_REQ_OK":
                    requested_user_port = Integer.parseInt(bodyFields[0]);
                    reqAddress = bodyFields[1];
                    personal_port = Integer.parseInt(bodyFields[2]);
                    msg = new ChatReqOkMessage(header, requested_user);
                    break;
                case "USERS_ONLINE":

                    // according to chatty the toString() of an Arraylist returns [element1,
                    // element2]
                    // therefor the following method should work

                    // String raw = bodyFields[0];
                    // raw = raw.trim();

                    // if (raw.startsWith("[") && raw.endsWith("]")) {
                    // raw = raw.substring(1, raw.length() - 1);

                    // if (!raw.isBlank()) {
                    // for (String u : raw.split(",")) {
                    // onlineUsers.add(u.trim());
                    // }
                    // }
                    // }....

                    for (String u : bodyFields) { // not sure if this works because i can't debug the list atm and i
                                                  // don't know wether we get a lot of bodyfields or just one containing
                                                  // all the names
                        onlineUsers.add(u);
                    }
                    msg = new UsersOnlineMessage(header, onlineUsers);
                    break;
                case "SEND_PORT":
                    port = Integer.parseInt(bodyFields[0]);
                    username = bodyFields[1];
                    msg = new SendPortMessage(header, port, publicKey, ipAddress);
                    break;
                case "CHAT_REQ_DENIED":
                    requested_user = bodyFields[0];
                    msg = new ChatReqDeniedMessage(header, requested_user);
                    break;
                case "ERROR":
                    errorCode = Integer.parseInt(bodyFields[0]);
                    msg = new ErrorMessage(header, errorCode);
                    break;
                case "FWD_CHAT_REQ":
                    requested_user = bodyFields[0];
                    msg = new ForwardChatRequestMessage(header, requested_user);
                    break;
                case "CHAT_MSG":
                    text = String.join(" ", bodyFields);
                    msg = new ChatMessage(header, text);
                    return (msg);
                default:
                    throw new Exception("Unsupported Body-Field");
            }

        }

        return msg;

    }
}
