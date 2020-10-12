package com.wnzhong.disruptor.bean;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class RingBufferCmd {

    public int code;

    public String msg;
}
