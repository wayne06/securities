package com.wnzhong.counter.config;

import com.alipay.remoting.exception.CodecException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import thirdpart.bean.CommonMsg;
import thirdpart.order.OrderCmd;
import thirdpart.tcp.TcpDirectSender;
import thirdpart.uuid.MyUuid;

import javax.annotation.PostConstruct;

import static thirdpart.bean.MsgConstants.COUNTER_NEW_ORDER;
import static thirdpart.bean.MsgConstants.NORMAL;

@Log4j2
@Configuration
public class GatewayConnection {

    @Autowired
    private CounterConfig counterConfig;

    private TcpDirectSender tcpDirectSender;

    @PostConstruct
    private void init() {
        tcpDirectSender = new TcpDirectSender(counterConfig.getSendIp(), counterConfig.getSendPort(), counterConfig.getVertx());
        tcpDirectSender.startup();
    }

    public void sendOrder(OrderCmd orderCmd) {
        byte[] data = null;
        try {
            data = counterConfig.getBodyCodec().serialize(orderCmd);
        } catch (CodecException e) {
            log.error("OrderCmd [{}] serializing error", orderCmd, e);
            return;
        }
        CommonMsg commonMsg = new CommonMsg();
        commonMsg.setBodyLength(data.length);
        commonMsg.setChecksum(counterConfig.getChecksum().getSum(data));
        commonMsg.setMsgSrc(counterConfig.getId());
        commonMsg.setMsgDst(counterConfig.getGatewayId());
        commonMsg.setMsgType(COUNTER_NEW_ORDER);
        commonMsg.setStatus(NORMAL);
        commonMsg.setMsgNo(MyUuid.getInstance().getUuid());
        commonMsg.setBody(data);
        tcpDirectSender.send(counterConfig.getMsgCodec().encodeToBuffer(commonMsg));
    }

    /**
     * @param data 把orderCmd序列化成的字节数组
     * @param msgSrc
     * @param msgDst
     * @param msgType
     * @param status
     * @param packetNo
     * @return
     */
    private CommonMsg genMsg(byte[] data, short msgSrc, short msgDst, short msgType, byte status, long packetNo) {
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
