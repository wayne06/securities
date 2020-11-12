package com.wnzhong.engine.handler.pub;

import com.alipay.remoting.exception.CodecException;
import com.wnzhong.engine.bean.command.RbCmd;
import com.wnzhong.engine.bean.orderbook.MatchEvent;
import com.wnzhong.engine.config.EngineConfig;
import com.wnzhong.engine.handler.BaseHandler;
import io.netty.util.collection.IntObjectHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.api.tuple.primitive.ShortObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import thirdpart.bean.CommonMsg;
import thirdpart.hq.L1MarketData;
import thirdpart.hq.MatchData;
import thirdpart.order.CmdType;

import java.util.List;

import static thirdpart.bean.MsgConstants.*;

/**
 * 行情处理器：
 * 1.往外广播行情
 * 2.把每个柜台发过来的有成交变化的推过去
 * @author wayne
 */
@Log4j2
@RequiredArgsConstructor
public class L1PubHandler extends BaseHandler {

    public static final int HQ_PUB_RATE = 5000;

    @NonNull
    private final ShortObjectHashMap<List<MatchData>> matcherEventMap;

    @NonNull
    private EngineConfig engineConfig;

    @Override
    public void onEvent(RbCmd cmd, long sequence, boolean endOfBatch) throws Exception {
        final CmdType cmdType = cmd.command;
        if (cmdType == CmdType.NEW_ORDER || cmdType == CmdType.CANCEL_ORDER) {
            for (MatchEvent e : cmd.matchEventList) {
                matcherEventMap.get(e.mid).add(e.copy());
            }
        } else if (cmdType == CmdType.HQ_PUB) {
            //1.五档行情
            pubMarketData(cmd.marketDataMap);
            //2.给各个柜台分别发送 MatchData
            pubMatcherData();
        }
    }

    private void pubMatcherData() {
        if (matcherEventMap.size() == 0) {
            return;
        }
        //log.info(matcherEventMap);
        try {
            for (ShortObjectPair<List<MatchData>> s : matcherEventMap.keyValuesView()) {
                if (CollectionUtils.isEmpty(s.getTwo())) {
                    continue;
                }
                byte[] serialize = engineConfig.getBodyCodec()
                        .serialize(s.getTwo().toArray(new MatchData[0]));
                pubData(serialize, s.getOne(), MATCH_ORDER_DATA);
                //清空已发送数据
                s.getTwo().clear();
            }
        } catch (CodecException e) {
            e.printStackTrace();
        }
    }

    public static final short HQ_ADDRESS = -1;

    private void pubMarketData(IntObjectHashMap<L1MarketData> marketDataMap) {
        log.info("------------marketDataMap" + marketDataMap);
        byte[] serialize = null;
        try {
            serialize = engineConfig.getBodyCodec()
                    .serialize(marketDataMap.values().toArray(new L1MarketData[0]));
        } catch (CodecException e) {
            e.printStackTrace();
        }
        if (serialize == null) {
            return;
        }
        pubData(serialize, HQ_ADDRESS, MATCH_HQ_DATA);
    }

    private void pubData(byte[] serialize, short dst, short msgType) {
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(serialize.length);
        msg.setChecksum(engineConfig.getCheckSum().getSum(serialize));
        msg.setMsgSrc(engineConfig.getId());
        msg.setMsgDst(dst);
        msg.setMsgType(msgType);
        msg.setStatus(NORMAL);
        msg.setBody(serialize);
        engineConfig.getBusSender().publish(msg);
    }
}
