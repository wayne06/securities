package com.wnzhong.engine.core;

import com.lmax.disruptor.RingBuffer;
import com.wnzhong.engine.bean.command.RbCmd;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.order.OrderCmd;

/**
 * @author wayne
 */
@Log4j2
@AllArgsConstructor
public class EngineApi {

    private final RingBuffer<RbCmd> ringBuffer;

    public void submitCommand(OrderCmd orderCmd) {

    }

}
