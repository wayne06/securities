package com.wnzhong.counter.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import thirdparty.bean.CommonMsg;

@Log4j2
@Configuration
public class GatewayConnection {

    @Autowired
    private CounterConfig counterConfig;

    /**
     * @param data 把orderCmd序列化成的字节数组
     * @param msgSrc
     * @param msgDst
     * @param msgType
     * @param status
     * @param packetNo
     * @return
     */
    private CommonMsg getMsg(byte[] data, short msgSrc, short msgDst, short msgType, byte status, long packetNo) {
        if (data == null) {
            log.error("Empty data.");
            return null;
        }
        CommonMsg commonMsg = new CommonMsg();
        commonMsg.setBodyLength(data.length);
        commonMsg.setChecksum(counterConfig.getChecksum().getSum(data));
        commonMsg.setMsgSrc(msgSrc);
        commonMsg.setMsgDst(msgDst);
        commonMsg.setMsgType(msgType);
        commonMsg.setStatus(status);
        commonMsg.setMsgNo(packetNo);
        commonMsg.setBody(data);
        return commonMsg;
    }

}
