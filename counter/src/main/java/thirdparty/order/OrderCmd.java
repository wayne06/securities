package thirdparty.order;

import lombok.Builder;
import lombok.ToString;

import java.io.Serializable;

@Builder
@ToString
public class OrderCmd implements Serializable {

    public CmdType type;

    public long timestamp;

    /**
     * 会员ID：交易所的每家会员的id，不同的会员的柜台id是有区别的，涉及到盘后数据的清算工作
     * 有了这个字段，交易所的撮合核心就能知道这笔单是从哪家券商或哪家机构发过来的
     */
    final public short mid;

    /**
     * 用户ID
     */
    final public long uid;

    /**
     * 代码
     */
    final public int code;

    /**
     * 方向
     */
    final public OrderDirection direction;

    /**
     * 价格
     */
    final public long price;

    /**
     * 量
     */
    final public long volume;

    /**
     * 委托类型
     * 1.LIMIT
     */
    final public OrderType orderType;

    /**
     * 委托编号
     */
    public long oid;


}
