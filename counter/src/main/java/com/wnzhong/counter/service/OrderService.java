package com.wnzhong.counter.service;

import com.wnzhong.counter.bean.OrderInfo;
import com.wnzhong.counter.bean.PosiInfo;
import com.wnzhong.counter.bean.TradeInfo;

import java.util.List;

public interface OrderService {

    Long getBalance(long uid);

    List<PosiInfo> getPosi(long uid);

    List<OrderInfo> getOrder(long uid);

    List<TradeInfo> getTrade(long uid);

}