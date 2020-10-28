package com.wnzhong.engine.bean.orderbook;

import lombok.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import thirdpart.order.OrderDirection;

/**
 * 只在 OrderBook 内部使用
 * @author wayne
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Order {

    private short mid;

    private long uid;

    private int code;

    private OrderDirection direction;

    private long price;

    private long volume;

    private long tvolume;

    private long oid;

    private long timestamp;

    private long innerOid;

    /**
     * timestamp is not included into hashCode() and equals() for repeatable results.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Order order = (Order) o;

        return new EqualsBuilder()
                .append(mid, order.mid)
                .append(uid, order.uid)
                .append(code, order.code)
                .append(direction, order.direction)
                .append(price, order.price)
                .append(volume, order.volume)
                .append(tvolume, order.tvolume)
                .append(oid, order.oid)
                //.append(timestamp, order.timestamp)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(mid)
                .append(uid)
                .append(code)
                .append(direction)
                .append(price)
                .append(volume)
                .append(tvolume)
                .append(oid)
                //.append(timestamp)
                .toHashCode();
    }
}
