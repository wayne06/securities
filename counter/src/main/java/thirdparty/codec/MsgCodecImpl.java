package thirdparty.codec;

import io.vertx.core.buffer.Buffer;
import thirdparty.bean.CommonMsg;

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
        int bodyLength = buffer.getInt(0);
        byte checkSum = buffer.getByte(4);
        short msgSrc = buffer.getShort(5);
        short msgDst = buffer.getShort(7);
        short msgType = buffer.getShort(9);
        byte status = buffer.getByte(11);
        long packetNo = buffer.getLong(12);
        byte[] body = buffer.getBytes(20, 20 + bodyLength);

        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(bodyLength);
        msg.setChecksum(checkSum);
        msg.setMsgSrc(msgSrc);
        msg.setMsgDst(msgDst);
        msg.setMsgType(msgType);
        msg.setStatus(status);
        msg.setMsgNo(packetNo);
        msg.setBody(body);
        return msg;
    }
}
