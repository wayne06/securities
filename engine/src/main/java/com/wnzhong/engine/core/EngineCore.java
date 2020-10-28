package com.wnzhong.engine.core;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.wnzhong.engine.bean.RbCmdFactory;
import com.wnzhong.engine.bean.command.RbCmd;
import com.wnzhong.engine.handler.BaseHandler;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

@Log4j2
public class EngineCore {

    private final Disruptor<RbCmd> disruptor;

    private static final int RING_BUFFER_SIZE = 1024;

    @Getter
    private final EngineApi api;

    public EngineCore(BaseHandler riskHandler, BaseHandler matchHandler, BaseHandler pubHandler) {
        this.disruptor = new Disruptor<RbCmd>(
                new RbCmdFactory(),
                RING_BUFFER_SIZE,
                new AffinityThreadFactory("aft_engine_core", AffinityStrategies.ANY),
                ProducerType.SINGLE,
                new BlockingWaitStrategy()
        );
        this.api = new EngineApi(disruptor.getRingBuffer());
    }
}
