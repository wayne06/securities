package com.wnzhong.counter.bean.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用返回格式
 */
@AllArgsConstructor
public class CounterRes {

    @Getter
    private int code;

    @Getter
    private String message;

    @Getter
    private Object data;

    public CounterRes(Object data) {
        this(0,"", data);
    }
}
