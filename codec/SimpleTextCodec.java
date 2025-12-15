package codec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import messages.Message;
import messages.MsgHeader;
import messages.MsgType;


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

        for (String part : bodyLines) {

            String command = part.split(" ")[0];
            String bodyFields[] = part.split(" ");
            bodyFields = Arrays.copyOfRange(bodyFields, 1, bodyFields.length); // remove first word

            switch (command) {
                case "PING":

                    msg = new Ping(header);
                    break;

                default:
                    throw new Exception("Unsupported Body-Field");
            }

        }

        return msg;

    }
}
