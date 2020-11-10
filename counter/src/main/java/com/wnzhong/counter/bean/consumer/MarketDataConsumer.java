package com.wnzhong.counter.bean.consumer;

import com.alipay.remoting.exception.CodecException;
import com.wnzhong.counter.config.CounterConfig;
import com.wnzhong.counter.util.JsonUtil;
import io.netty.util.collection.IntObjectHashMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thirdpart.hq.L1MarketData;

import javax.annotation.PostConstruct;

import static com.wnzhong.counter.bean.consumer.MqttBusConsumer.INNER_MARKET_DATA_CACHE_ADDR;
import static com.wnzhong.counter.config.WebSocketConfig.L1_MARKET_DATA_PREFIX;

/**
 * @author wayne
 */
@Log4j2
@Component
public class MarketDataConsumer {

    @Autowired
    private CounterConfig counterConfig;

    /**
     * <code, 最新的五档行情>
     */
    private IntObjectHashMap<L1MarketData> l1Cache = new IntObjectHashMap<>();

    @PostConstruct
    private void init() {
        EventBus eventBus = counterConfig.getVertx().eventBus();

        //处理核心发过来的行情
        eventBus.consumer(INNER_MARKET_DATA_CACHE_ADDR).handler(buffer -> {
            Buffer body = (Buffer) buffer.body();
            if (body.length() == 0) {
                return;
            }
            L1MarketData[] marketDataArr = null;
            try {
                marketDataArr = counterConfig.getBodyCodec().deserialize(body.getBytes(), L1MarketData[].class);
            } catch (CodecException e) {
                log.error(e);
            }
            if (ArrayUtils.isEmpty(marketDataArr)) {
                return;
            }
            for (L1MarketData marketData : marketDataArr) {
                L1MarketData l1MarketData = l1Cache.get(marketData.code);
                if (l1MarketData == null || l1MarketData.timestamp < marketData.timestamp) {
                    l1Cache.put(marketData.code, marketData);
                } else {
                    log.error("L1MarketData is null or L1MarketData.timestamp < marketData.timestamp");
                }
            }
        });
        eventBus.consumer(L1_MARKET_DATA_PREFIX).handler(h -> {
            int code = Integer.parseInt(h.headers().get("code"));
            L1MarketData l1MarketData = l1Cache.get(code);
            h.reply(JsonUtil.toJson(l1MarketData));
        });
    }

}
