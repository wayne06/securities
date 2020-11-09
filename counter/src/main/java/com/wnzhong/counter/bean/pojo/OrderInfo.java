package com.wnzhong.counter.bean.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderInfo {

    private int id;
    private long uid;
    private int code;
    private String name;
    private int direction;
    private int type;
    private long price;
    private long ocount;
    private int status;
    private String date;
    private String time;

}
