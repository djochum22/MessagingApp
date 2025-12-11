package messages;

public class MsgHeader {
    private final MsgType type;
    private final String msgId;
    private final String correlationId;
    private final long timestampMillis;

    public MsgHeader(MsgType type, String msgId, String correlationId, long timestampMillis) {
        this.type = type;
        this.msgId = msgId;
        this.correlationId = correlationId;
        this.timestampMillis = timestampMillis;
    }

    public MsgType type() {
        return type;
    }

    public String msgId() {
        return msgId;
    }

    public String correlationId() {
        return correlationId;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    @Override
    public String toString() {
        return String.format(
                "Type: %s\r\nMsgId: %s\r\nCorrelationId: %s\r\nTimestamp in Millis: %d",
                type, msgId, correlationId, timestampMillis);
    }

}
