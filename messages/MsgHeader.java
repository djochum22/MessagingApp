package messages;

public class MsgHeader {
    private final MsgType type;
    private final int msgId;
    private final int correlationId;
    private final long timestampMillis;

    public MsgHeader(MsgType type, int msgId, int correlationId, long timestampMillis) {
        this.type = type;
        this.msgId = msgId;
        this.correlationId = correlationId;
        this.timestampMillis = timestampMillis;
    }

    public MsgType type() {
        return type;
    }

    public int msgId() {
        return msgId;
    }

    public int correlationId() {
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
