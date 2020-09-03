package com.wnzhong.counter.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TradeInfo {

    private int id;
    private long uid;
    private int code;
    private String name;
    private int direction;
    private long price;
    private long tcount;
    private int oid;
    private String date;
    private String time;

}
