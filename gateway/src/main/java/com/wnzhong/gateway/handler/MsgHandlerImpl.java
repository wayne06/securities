package com.wnzhong.gateway.handler;

import com.alipay.remoting.exception.CodecException;
import com.wnzhong.gateway.bean.OrderCmdContainer;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;
import thirdpart.codec.BodyCodec;
import thirdpart.order.OrderCmd;

@Log4j2
@AllArgsConstructor
public class MsgHandlerImpl implements MsgHandler {

    private BodyCodec bodyCodec;

    @Override
    public void onCounterData(CommonMsg commonMsg) {
        OrderCmd orderCmd;
        try {
            orderCmd = bodyCodec.deserialize(commonMsg.getBody(), OrderCmd.class);
            log.info("Recv cmd: {}", orderCmd);
            // 生产中数据量大，万不可使用 log.info；使用 log.debug 的正确方式如下：
            //if (log.isDebugEnabled()) {
            //    log.debug("recv cmd: {}", orderCmd);
            //}
            if (!OrderCmdContainer.getInstance().cache(orderCmd)) {
                log.error("Gateway queue insert fail. Queue length: {}, Order: {}",
                        OrderCmdContainer.getInstance().size(), orderCmd);
            }


        } catch (CodecException e) {
            log.error("Decode order command error: ", e);
        }
    }
}
