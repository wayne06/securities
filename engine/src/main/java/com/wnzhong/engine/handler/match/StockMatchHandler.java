package com.wnzhong.engine.handler.match;

import com.wnzhong.engine.bean.command.CmdResultCode;
import com.wnzhong.engine.bean.command.RbCmd;
import com.wnzhong.engine.bean.orderbook.IOrderBook;
import com.wnzhong.engine.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * @author wayne
 */
@RequiredArgsConstructor
public class StockMatchHandler extends BaseHandler {

    @NonNull
    private final IntObjectHashMap<IOrderBook> orderBookMap;

    @Override
    public void onEvent(RbCmd cmd, long sequence, boolean endOfBatch) throws Exception {
        if (cmd.resultCode.getCode() < 0) {
            //风控未通过
            return;
        }
        cmd.resultCode = processCmd(cmd);
    }

    private CmdResultCode processCmd(RbCmd cmd) {
        switch (cmd.command) {
            case NEW_ORDER:
                return orderBookMap.get(cmd.code).newOrder(cmd);
            case CANCEL_ORDER:
                return orderBookMap.get(cmd.code).cancelOrder(cmd);
            case HQ_PUB:
                orderBookMap.forEachKeyValue((code, orderBook) ->
                        cmd.marketDataMap.put(code, orderBook.getL1MarketDataSnapshot()));
                return CmdResultCode.SUCCESS;
            default:
                return CmdResultCode.SUCCESS;
        }
    }
}
