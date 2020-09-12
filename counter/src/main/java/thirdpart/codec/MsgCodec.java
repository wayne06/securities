package thirdpart.codec;

import io.vertx.core.buffer.Buffer;
import thirdpart.bean.CommonMsg;

/**
 * CommonMsg 和 TCP 流的互转接口
 * @author wayne
 */
public interface MsgCodec {

    /**
     * CommonMsg => TCP
     * @param commonMsg
     * @return
     */
    Buffer encodeToBuffer(CommonMsg commonMsg);

    /**
     * TCP => CommonMsg
     * @param buffer
     * @return
     */
    CommonMsg decodeFromBuffer(Buffer buffer);

}
