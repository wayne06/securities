package com.wnzhong.counter.bean.consumer;

import com.alipay.remoting.exception.CodecException;
import com.google.common.collect.ImmutableMap;
import com.wnzhong.counter.config.CounterConfig;
import com.wnzhong.counter.util.DbUtil;
import com.wnzhong.counter.util.IDConverter;
import com.wnzhong.counter.util.JsonUtil;
import io.netty.util.collection.LongObjectHashMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import thirdpart.hq.MatchData;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderStatus;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wnzhong.counter.bean.consumer.MqttBusConsumer.INNER_MATCH_DATA_ADDR;
import static com.wnzhong.counter.config.WebSocketConfig.ORDER_NOTIFY_ADDR_PREFIX;
import static com.wnzhong.counter.config.WebSocketConfig.TRADE_NOTIFY_ADDR_PREFIX;
import static thirdpart.order.OrderDirection.*;
import static thirdpart.order.OrderStatus.*;

/**
 * @author wayne
 */
@Log4j2
@Component
public class MatchDataConsumer {

    public static final String ORDER_DATA_CACHE_ADDR = "order_data_cache_addr";

    @Autowired
    private CounterConfig counterConfig;

    /**
     * <委托编号, OrderCmd>
     */
    private LongObjectHashMap<OrderCmd> oidOrderMap = new LongObjectHashMap<>();

    @PostConstruct
    private void init() {
        EventBus eventBus = counterConfig.getVertx().eventBus();

        //接收委托缓存（从终端来）
        eventBus.consumer(ORDER_DATA_CACHE_ADDR).handler(buffer -> {
            Buffer body = (Buffer) buffer.body();
            try {
                OrderCmd orderCmd = counterConfig.getBodyCodec().deserialize(body.getBytes(), OrderCmd.class);
                log.info("Cache order: {}", orderCmd);
                oidOrderMap.put(orderCmd.oid, orderCmd);
            } catch (CodecException e) {
                log.error(e);
            }
        });
        eventBus.consumer(INNER_MATCH_DATA_ADDR).handler(buffer -> {
            //根据长度判断
            Buffer body = (Buffer) buffer.body();
            if (body.length() == 0) {
                return;
            }
            MatchData[] matchDataArr = null;
            try {
                matchDataArr = counterConfig.getBodyCodec().deserialize(body.getBytes(), MatchData[].class);
            } catch (CodecException e) {
                log.error(e);
            }
            if (ArrayUtils.isEmpty(matchDataArr)) {
                return;
            }

            //按照 oid 进行分类
            Map<Long, List<MatchData>> collect =
                    Arrays.asList(matchDataArr).stream().collect(Collectors.groupingBy(t -> t.oid));
            for (Map.Entry<Long, List<MatchData>> entry : collect.entrySet()) {
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    continue;
                }
                //拆分获取柜台内部委托编号
                long oid = entry.getKey();
                int counterOId = IDConverter.seperateLong2Int(oid)[1];
                //高位：柜台； 低位：内部ID
                updateAndNotify(counterOId, entry.getValue(), oidOrderMap.get(oid));
            }
        });
    }

    private void updateAndNotify(int counterOId, List<MatchData> value, OrderCmd orderCmd) {
        if (CollectionUtils.isEmpty(value)) {
            return;
        }
        //成交
        for (MatchData matchData : value) {
            OrderStatus status = matchData.status;
            if (status == TRADE_ED || status == PART_TRADE) {
                //更新成交
                DbUtil.saveTrade(counterOId, matchData, orderCmd);
                //持仓 资金 多退少补
                if (orderCmd.direction == BUY) {
                    //B 13 30股   10 10股
                    if (orderCmd.price > matchData.price) {
                        DbUtil.updateBalance(orderCmd.uid, (orderCmd.price - matchData.price) * matchData.volume);
                    }
                    DbUtil.updateOrCreatePosi(orderCmd.uid, orderCmd.code, matchData.volume, matchData.price);
                } else if (orderCmd.direction == SELL) {
                    DbUtil.updateBalance(orderCmd.uid, matchData.price * matchData.volume);
                } else {
                    log.error("Wrong direction [{}]", orderCmd.direction);
                }
                //通知客户端
                counterConfig.getVertx().eventBus().publish(
                        TRADE_NOTIFY_ADDR_PREFIX + orderCmd.uid,
                        JsonUtil.toJson(ImmutableMap.of("code", orderCmd.code,
                                                        "direction", orderCmd.direction,
                                                        "volume", matchData.volume))
                );
            }
        }

        //委托变动：根据最后一笔 Match 处理委托
        MatchData finalMatchData = value.get(value.size() - 1);
        OrderStatus finalOrderStatus = finalMatchData.status;
        DbUtil.updateOrder(orderCmd.uid, counterOId, finalOrderStatus);
        if (finalOrderStatus == CANCEL_ED || finalOrderStatus == PART_CANCEL) {
            oidOrderMap.remove(orderCmd.oid);
            if (orderCmd.direction == BUY) {
                //撤买单：增加资金
                DbUtil.updateBalance(orderCmd.uid, -(orderCmd.price * finalMatchData.volume));
            } else if (orderCmd.direction == SELL) {
                //撤卖单：增加持仓
                DbUtil.updateOrCreatePosi(orderCmd.uid, orderCmd.code, -finalMatchData.volume, orderCmd.price);
            } else {
                log.error("Wrong direction [{}]", orderCmd.direction);
            }
        }

        //通知委托终端
        counterConfig.getVertx().eventBus().publish(
                ORDER_NOTIFY_ADDR_PREFIX + orderCmd.uid,
                "");

    }


}
