package com.wnzhong.engine.bean;

import com.google.common.collect.Lists;
import com.lmax.disruptor.EventFactory;
import com.wnzhong.engine.bean.command.CmdResultCode;
import com.wnzhong.engine.bean.command.RbCmd;
import io.netty.util.collection.IntObjectHashMap;

/**
 * @author wayne
 */
public class RbCmdFactory implements EventFactory<RbCmd> {

    @Override
    public RbCmd newInstance() {
        return RbCmd.builder()
                .resultCode(CmdResultCode.SUCCESS)
                .matchEventList(Lists.newArrayList())
                .marketDataMap(new IntObjectHashMap<>())
                .build();
    }
}
