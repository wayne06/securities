package com.wnzhong.counter.service;

import com.wnzhong.counter.bean.pojo.OrderInfo;
import com.wnzhong.counter.bean.pojo.PosiInfo;
import com.wnzhong.counter.bean.pojo.TradeInfo;
import com.wnzhong.counter.config.CounterConfig;
import thirdpart.order.OrderCmd;

import java.util.List;

public interface OrderService {

    Long getBalance(long uid);

    List<PosiInfo> getPosi(long uid);

    List<OrderInfo> getOrder(long uid);

    List<TradeInfo> getTrade(long uid);

    boolean sendOrder(OrderCmd orderCmd, CounterConfig counterConfig);

    boolean cancelOrder(OrderCmd orderCmd);
}
