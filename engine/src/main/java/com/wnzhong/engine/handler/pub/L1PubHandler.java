package com.wnzhong.engine.handler.pub;

import com.wnzhong.engine.bean.command.RbCmd;
import com.wnzhong.engine.config.EngineConfig;
import com.wnzhong.engine.handler.BaseHandler;
import io.netty.util.collection.ShortObjectHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.hq.MatchData;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class L1PubHandler extends BaseHandler {

    @NonNull
    private final ShortObjectHashMap<List<MatchData>> matcherEventMap;

    @NonNull
    private EngineConfig engineConfig;

    @Override
    public void onEvent(RbCmd event, long sequence, boolean endOfBatch) throws Exception {

    }
}
