package com.wnzhong.engine.bean.command;

import com.wnzhong.engine.bean.orderbook.MatchEvent;
import io.netty.util.collection.IntObjectHashMap;
import lombok.Builder;
import lombok.ToString;
import thirdpart.hq.L1MarketData;
import thirdpart.order.CmdType;
import thirdpart.order.OrderDirection;
import thirdpart.order.OrderType;

import java.util.List;

/**
 * 把所有通过 disruptor 来运转的数据全部放进来
 * @author wayne
 */
@Builder
@ToString
public class RbCmd {

    public long timestamp;
    public short mid;
    public long uid;
    public CmdType command;
    public int code;
    public OrderDirection direction;
    public long price;
    public long volume;
    public long oid;
    public OrderType orderType;

    //保存撮合结果
    public List<MatchEvent> matchEventList;

    //前置风控 - 撮合 - 发布
    public CmdResultCode resultCode;

    //保存行情
    public IntObjectHashMap<L1MarketData> marketDataMap;

}
