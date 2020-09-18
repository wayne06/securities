package thirdpart.codec;

import io.vertx.core.buffer.Buffer;
import thirdpart.bean.CommonMsg;

public class MsgCodecImpl implements MsgCodec {

    @Override
    public Buffer encodeToBuffer(CommonMsg msg) {
        return Buffer.buffer()
                .appendInt(msg.getBodyLength())
                .appendByte(msg.getChecksum())
                .appendShort(msg.getMsgSrc())
                .appendShort(msg.getMsgDst())
                .appendShort(msg.getMsgType())
                .appendByte(msg.getStatus())
                .appendLong(msg.getMsgNo())
                .appendBytes(msg.getBody());
    }

    @Override
    public CommonMsg decodeFromBuffer(Buffer buffer) {
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(buffer.getInt(0));
        msg.setChecksum(buffer.getByte(4));
        msg.setMsgSrc(buffer.getShort(5));
        msg.setMsgDst(buffer.getShort(7));
        msg.setMsgType(buffer.getShort(9));
        msg.setStatus(buffer.getByte(11));
        msg.setMsgNo(buffer.getLong(12));
        msg.setBody(buffer.getBytes(20, 20 + buffer.getInt(0)));
        return msg;
    }
}
