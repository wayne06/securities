package com.wnzhong.engine.bean.orderbook;

import com.wnzhong.engine.bean.command.CmdResultCode;
import com.wnzhong.engine.bean.command.RbCmd;
import thirdpart.hq.L1MarketData;

import static thirdpart.hq.L1MarketData.L1_SIZE;

/**
 * @author wayne
 */
public interface IOrderBook {

    ///////////////////////////////////////////新增委托//////////////////////////////////////////////////

    /**
     * 新增委托
     * @param rbCmd
     * @return
     */
    CmdResultCode newOrder(RbCmd rbCmd);

    /////////////////////////////////////////////撤单////////////////////////////////////////////////////
    /**
     * 撤单
     * @param rbCmd
     * @return
     */
    CmdResultCode cancelOrder(RbCmd rbCmd);

    /////////////////////////////////////////查询行情快照/////////////////////////////////////////////////

    /**
     * 查询行情快照
     * @return
     */
    default L1MarketData getL1MarketDataSnapshot() {
        final int buySize = limitBuyBucketSize(L1_SIZE);
        final int sellSize = limitSellBucketSize(L1_SIZE);
        final L1MarketData data = new L1MarketData(buySize, sellSize);
        fillBuys(buySize, data);
        fillSells(sellSize, data);
        fillCode(data);
        data.timestamp = System.currentTimeMillis();
        return data;
    }

    void fillCode(L1MarketData data);

    void fillSells(int sellSize, L1MarketData data);

    void fillBuys(int buySize, L1MarketData data);

    int limitSellBucketSize(int maxSize);

    int limitBuyBucketSize(int maxSize);

    //////////////////////////////////////////初始化枚举//////////////////////////////////////////////////

    //todo


}
