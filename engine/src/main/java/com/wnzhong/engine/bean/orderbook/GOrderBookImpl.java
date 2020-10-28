package com.wnzhong.engine.bean.orderbook;

import com.google.common.collect.Lists;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.wnzhong.engine.bean.command.CmdResultCode;
import com.wnzhong.engine.bean.command.RbCmd;
import io.netty.util.collection.LongObjectHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.hq.L1MarketData;
import thirdpart.order.OrderDirection;
import thirdpart.order.OrderStatus;

import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

@Log4j2
@RequiredArgsConstructor
public class GOrderBookImpl implements IOrderBook {

    @NonNull
    private int code;

    /**
     * <价格，orderBucket>
     */
    private final NavigableMap<Long, IOrderBucket> sellBuckets = new TreeMap<>();

    private final NavigableMap<Long, IOrderBucket> buyBuckets = new TreeMap<>(Collections.reverseOrder());

    private final LongObjectHashMap<Order> oidMap = new LongObjectHashMap<>();

    @Override
    public CmdResultCode newOrder(RbCmd rbCmd) {
        //1.判断重复
        if (oidMap.containsKey(rbCmd.oid)) {
            return CmdResultCode.DUPLICATE_ORDER_ID;
        }

        //2.生成新 order
        //2.1 预撮合
        // s 50 100 买单buckets >= 50，所有OrderBucket
        // b 40 200 卖单buckets <= 40，符合条件
        NavigableMap<Long, IOrderBucket> subMatchBuckets =
                (rbCmd.direction == OrderDirection.SELL ? buyBuckets : sellBuckets)
                        .headMap(rbCmd.price, true);
        long tVolume = preMatch(rbCmd, subMatchBuckets);
        if (tVolume == rbCmd.volume) {
            return CmdResultCode.SUCCESS;
        }
        final Order order = Order.builder()
                .mid(rbCmd.mid)
                .uid(rbCmd.uid)
                .code(rbCmd.code)
                .direction(rbCmd.direction)
                .price(rbCmd.price)
                .volume(rbCmd.volume)
                .tvolume(tVolume)
                .oid(rbCmd.oid)
                .timestamp(rbCmd.timestamp)
                .build();

        if (tVolume == 0) {
            genMatchEvent(rbCmd, OrderStatus.ORDER_ED);
        } else {
            genMatchEvent(rbCmd, OrderStatus.PART_TRADE);
        }

        //3.加入 orderBucket
        final IOrderBucket bucket =
                (rbCmd.direction == OrderDirection.SELL ? sellBuckets : buyBuckets)
                        .computeIfAbsent(rbCmd.price, p -> {
                            final IOrderBucket b = IOrderBucket.create(IOrderBucket.OrderBucketImplType.WN);
                            b.setPrice(p);
                            return b;
                        });
        bucket.put(order);
        oidMap.put(rbCmd.oid, order);

        return CmdResultCode.SUCCESS;
    }

    private long preMatch(RbCmd cmd, NavigableMap<Long, IOrderBucket> matchingBuckets) {
        int tVol = 0;
        if (matchingBuckets.size() == 0) {
            return tVol;
        }

        List<Long> emptyBuckets = Lists.newArrayList();
        for (IOrderBucket bucket : matchingBuckets.values()) {
            tVol += bucket.match(cmd.volume - tVol, cmd, order -> oidMap.remove(order.getOid()));
            if (bucket.getTotalVolume() == 0) {
                emptyBuckets.add(bucket.getPrice());
            }
            if (tVol == cmd.volume) {
                break;
            }
        }
        emptyBuckets.forEach(matchingBuckets::remove);

        return tVol;
    }

    @Override
    public CmdResultCode cancelOrder(RbCmd rbCmd) {
        //1.从缓存中移除委托
        Order order = oidMap.get(rbCmd.oid);
        if (order == null) {
            return CmdResultCode.INVALID_ORDER_ID;
        }
        oidMap.remove(order.getOid());

        //2.从 OrderBucket 中移除委托
        final NavigableMap<Long, IOrderBucket> buckets =
                order.getDirection() == OrderDirection.SELL ? sellBuckets : buyBuckets;
        IOrderBucket orderBucket = buckets.get(order.getPrice());
        orderBucket.remove(order.getOid());
        if (orderBucket.getTotalVolume() == 0) {
            buckets.remove(order.getPrice());
        }

        //3.发送扯断 MatchEvent
        MatchEvent cancelEvent = new MatchEvent();
        cancelEvent.timestamp = System.currentTimeMillis();
        cancelEvent.mid = order.getMid();
        cancelEvent.oid = order.getOid();
        cancelEvent.status = order.getTvolume() == 0 ? OrderStatus.CANCEL_ED : OrderStatus.PART_CANCEL;
        cancelEvent.volume = order.getTvolume() - order.getVolume();
        rbCmd.matchEventList.add(cancelEvent);

        return CmdResultCode.SUCCESS;
    }

    @Override
    public void fillCode(L1MarketData data) {

    }

    @Override
    public void fillSells(int sellSize, L1MarketData data) {

    }

    @Override
    public void fillBuys(int buySize, L1MarketData data) {

    }

    @Override
    public int limitSellBucketSize(int maxSize) {
        return 0;
    }

    @Override
    public int limitBuyBucketSize(int maxSize) {
        return 0;
    }
}
