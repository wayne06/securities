package com.wnzhong.engine.core;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.wnzhong.engine.bean.RbCmdFactory;
import com.wnzhong.engine.bean.command.RbCmd;
import com.wnzhong.engine.handler.BaseHandler;
import com.wnzhong.engine.handler.exception.DisruptorExceptionHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import thirdpart.order.CmdType;
import thirdpart.order.OrderCmd;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import static com.wnzhong.engine.handler.pub.L1PubHandler.HQ_PUB_RATE;

/**
 * 定义 disruptor；把消费线程按顺序绑定进去；定时发布行情
 * @author wayne
 */
@Log4j2
public class EngineCore {

    private final Disruptor<RbCmd> disruptor;

    private static final int RING_BUFFER_SIZE = 1024;

    @Getter
    private final EngineApi api;

    public EngineCore(@NonNull BaseHandler riskHandler,
                      @NonNull BaseHandler matchHandler,
                      @NonNull BaseHandler pubHandler) {
        this.disruptor = new Disruptor<RbCmd>(
                new RbCmdFactory(),
                RING_BUFFER_SIZE,
                new AffinityThreadFactory("aft_engine_core", AffinityStrategies.ANY),
                ProducerType.SINGLE,
                new BlockingWaitStrategy()
        );
        this.api = new EngineApi(disruptor.getRingBuffer());

        //1.全局异常处理器
        final DisruptorExceptionHandler<RbCmd> exceptionHandler = new DisruptorExceptionHandler<>(
                "main",
                (ex, seq) -> log.error("Exception thrown on seq = {}", seq, ex)
        );
        disruptor.setDefaultExceptionHandler(exceptionHandler);

        //2.前置风控 -> 撮合 -> 发布数据
        disruptor.handleEventsWith(riskHandler).then(matchHandler).then(pubHandler);

        //3.启动
        disruptor.start();
        log.info("Match engine start.");

        //4.定时发布行情任务
        new Timer().schedule(new HqPubTask(), 5000, HQ_PUB_RATE);
    }

    private class HqPubTask extends TimerTask {
        @Override
        public void run() {
            api.submitCommand(OrderCmd.builder().type(CmdType.HQ_PUB).build());
        }
    }
}
