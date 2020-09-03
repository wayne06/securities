package com.wnzhong.counter.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PosiInfo {

    private int id;
    private long uid;
    private int code;
    private String name;
    private long cost;
    private long count;

}
