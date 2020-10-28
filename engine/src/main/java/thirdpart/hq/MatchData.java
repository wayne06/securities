package thirdpart.hq;

import lombok.Builder;
import thirdpart.order.OrderStatus;

import java.io.Serializable;

/**
 * 丢到总线，给柜台或其他订阅终端查看（模板类，对所有服务都公开）
 * @author wayne
 */
@Builder
public class MatchData implements Serializable {

    public long timestamp;

    public short mid;

    public long oid;

    public OrderStatus status;

    public long tid;

    //撤单数量 成交数量
    public long volume;

    public long price;

}
