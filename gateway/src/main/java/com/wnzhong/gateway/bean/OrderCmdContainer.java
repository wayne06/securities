package com.wnzhong.gateway.bean;

import com.google.common.collect.Lists;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class OrderCmdContainer {

    private static OrderCmdContainer container = new OrderCmdContainer();

    private OrderCmdContainer() {}

    public static OrderCmdContainer getInstance() {
        return container;
    }


    private final BlockingQueue<OrderCmd> orderCmdQueue = new LinkedBlockingDeque<>();

    public boolean cache(OrderCmd orderCmd) {
        return orderCmdQueue.offer(orderCmd);
    }

    public List<OrderCmd> getAll() {
        List<OrderCmd> orderCmdList = Lists.newArrayList();
        int count = orderCmdQueue.drainTo(orderCmdList);
        return count == 0 ? null : orderCmdList;
    }

    public int size() {
        return orderCmdQueue.size();
    }


}
