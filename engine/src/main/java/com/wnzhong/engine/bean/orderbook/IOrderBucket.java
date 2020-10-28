package com.wnzhong.engine.bean.orderbook;

import com.wnzhong.engine.bean.command.RbCmd;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public interface IOrderBucket extends Comparable<IOrderBucket> {

    AtomicLong tidGen = new AtomicLong(0);

    //新增订单
    void put(Order order);

    //移除订单
    Order remove(long oid);

    //match
    long match(long volumeLeft, RbCmd triggerCmd, Consumer<Order> removeOrderCallback);

    //行情发布
    long getPrice();
    void setPrice(long price);
    long getTotalVolume();

    //初始化选项
    static IOrderBucket create(OrderBucketImplType type) {
        switch (type) {
            case WN:
                return new GOrderBucketImpl();
            default:
                throw new IllegalArgumentException();
        }
    }

    enum OrderBucketImplType {
        /**
         *
         */
        WN(0);

        private byte code;

        OrderBucketImplType(int code) {
            this.code = (byte) code;
        }
    }


    //比较排序
    @Override
    default int compareTo(IOrderBucket other) {
        return Long.compare(this.getPrice(), other.getPrice());
    }

}
