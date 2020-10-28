package com.wnzhong.engine.bean.orderbook;

import lombok.NoArgsConstructor;
import lombok.ToString;
import thirdpart.hq.MatchData;
import thirdpart.order.OrderStatus;

/**
 * 在 disruptor 整个运转过程中内部的 event
 * @author wayne
 */
@NoArgsConstructor
@ToString
public class MatchEvent {

    public long timestamp;

    public short mid;

    public long oid;

    public OrderStatus status = OrderStatus.NOT_SET;

    public long tid;

    //撤单数量，成交数量
    public long volume;

    public long price;

    /**
     * 将 matchEvent 转换为 matchData
     * @return
     */
    public MatchData copy() {
        return MatchData.builder()
                .timestamp(this.timestamp)
                .mid(this.mid)
                .oid(this.oid)
                .status(this.status)
                .tid(this.tid)
                .volume(this.volume)
                .price(this.price)
                .build();

    }

}
