package com.wnzhong.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import com.wnzhong.disruptor.bean.RingBufferCmd;
import com.wnzhong.disruptor.bean.RingBufferData;
import com.wnzhong.disruptor.bean.RingBufferaCmdFactory;
import com.wnzhong.disruptor.exception.DisruptorExceptionHandler;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

import java.util.Timer;
import java.util.TimerTask;

@Log4j2
public class Test {

    public static void main(String[] args) {
        new Test().initDisruptor();
    }

    private Disruptor disruptor;

    public void initDisruptor() {
        disruptor = new Disruptor(
                // event factory
                new RingBufferaCmdFactory(),

                // 2^n，13544980 & (1024-1) 提高效率
                1024,

                // 线程池
                new AffinityThreadFactory("aft_core", AffinityStrategies.ANY),

                // 一个生产者线程
                ProducerType.SINGLE,

                // BusySpin：while(true) 适合延迟要求苛刻的场景，很耗CPU
                // YieldWait：轮询完成使用yield出让CPU资源，等待重新唤醒，折中
                // BlockWait/TimeoutBlockWait：加锁的Queue >> BlockingQueue
                new BlockingWaitStrategy()
        );

        // 设置全局异常处理器，在代码内部try-catch不合适
        final DisruptorExceptionHandler<RingBufferCmd> exceptionHandler =
                new DisruptorExceptionHandler<>("disruptor-1",
                        (ex, seq) -> {log.error("Exception thrown on seq={}", seq, ex);});
        disruptor.setDefaultExceptionHandler(exceptionHandler);

        // 定义消费和生产的关系
        ConsumerA consumerA = new ConsumerA();
        ConsumerB consumerB = new ConsumerB();
        disruptor.handleEventsWith(consumerA).then(consumerB);

        disruptor.start();

        // 发布数据
        new Timer().schedule(new ProducerTask(), 2000, 1000);
    }

    private static final EventTranslatorOneArg<RingBufferCmd, RingBufferData> PUB_TRANSLATOR =
            (ringBufferCmd, seq, ringBufferData) -> {
                ringBufferCmd.code = ringBufferData.code;
                ringBufferCmd.msg = ringBufferData.msg;
            };

    private int index = 0;

    private class ProducerTask extends TimerTask {
        @Override
        public void run() {
            disruptor.getRingBuffer().publishEvent(PUB_TRANSLATOR, new RingBufferData(index, "Hello Disruptor."));
            index++;
        }
    }

    private class ConsumerA implements EventHandler<RingBufferCmd> {
        @Override
        public void onEvent(RingBufferCmd ringBufferCmd, long sequence, boolean endOfBatch) throws Exception {
            log.info("ConsumerA receive: {}", ringBufferCmd);
        }
    }

    private class ConsumerB implements EventHandler<RingBufferCmd> {
        @Override
        public void onEvent(RingBufferCmd ringBufferCmd, long sequence, boolean endOfBatch) throws Exception {
            log.info("ConsumerB receive: {}", ringBufferCmd);
        }
    }


}
