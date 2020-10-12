package com.wnzhong.disruptor.bean;

import com.lmax.disruptor.EventFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RingBufferaCmdFactory implements EventFactory<RingBufferCmd> {

    @Override
    public RingBufferCmd newInstance() {
        return RingBufferCmd.builder()
                .code(0)
                .msg("")
                .build();
    }
}
