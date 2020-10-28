package com.wnzhong.engine.handler.match;

import com.wnzhong.engine.bean.command.RbCmd;
import com.wnzhong.engine.bean.orderbook.IOrderBook;
import com.wnzhong.engine.handler.BaseHandler;
import io.netty.util.collection.IntObjectHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StockMatchHandler extends BaseHandler {

    @NonNull
    private final IntObjectHashMap<IOrderBook> orderBookMap;

    @Override
    public void onEvent(RbCmd event, long sequence, boolean endOfBatch) throws Exception {

    }
}
