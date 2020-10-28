package com.wnzhong.engine.bean.orderbook;

import com.wnzhong.engine.bean.command.CmdResultCode;
import com.wnzhong.engine.bean.command.RbCmd;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.hq.L1MarketData;

@Log4j2
@RequiredArgsConstructor
public class GOrderBookImpl implements IOrderBook {

    @NonNull
    private int code;

    @Override
    public CmdResultCode newOrder(RbCmd rbCmd) {
        return null;
    }

    @Override
    public CmdResultCode cancelOrder(RbCmd rbCmd) {
        return null;
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
