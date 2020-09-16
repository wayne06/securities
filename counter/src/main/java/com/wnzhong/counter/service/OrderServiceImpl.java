package com.wnzhong.counter.service;

import com.wnzhong.counter.bean.OrderInfo;
import com.wnzhong.counter.bean.PosiInfo;
import com.wnzhong.counter.bean.TradeInfo;
import com.wnzhong.counter.config.GatewayConnection;
import com.wnzhong.counter.util.DbUtil;
import com.wnzhong.counter.util.IDConverter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderDirection;

import java.util.List;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private GatewayConnection gatewayConnection;

    @Override
    public Long getBalance(long uid) {
        return DbUtil.getBalance(uid);
    }

    @Override
    public List<PosiInfo> getPosi(long uid) {
        return DbUtil.getPosiList(uid);
    }

    @Override
    public List<OrderInfo> getOrder(long uid) {
        return DbUtil.getOrderList(uid);
    }

    @Override
    public List<TradeInfo> getTrade(long uid) {
        return DbUtil.getTradeList(uid);
    }

    @Override
    public boolean sendOrder(OrderCmd orderCmd) {
        // 1.入库
        int oid = DbUtil.saveOrder(orderCmd);

        // 2.发送到网关
        if (oid < 0) {
            return false;
        } else {
            // a.调整资金、持仓数据
            if (orderCmd.direction == OrderDirection.BUY) {
                // 减资金
                DbUtil.updateBalance(orderCmd.uid, - orderCmd.price * orderCmd.volume);
            } else if (orderCmd.direction == OrderDirection.SELL) {
                // 减持仓
                DbUtil.updateOrCreatePosi(orderCmd.uid, orderCmd.code, - orderCmd.volume, orderCmd.price);
            } else {
                log.error("Illegal direction [{}], orderCmd [{}]", orderCmd.direction, orderCmd);
                return false;
            }
            // b.组装全局ID：通过其知道该委托来自哪个柜台 long[柜台ID，委托ID]
            // 长整型高位用柜台ID，低位部分用这笔委托在数据库中的主键
            orderCmd.oid = IDConverter.combineInt2Long(orderCmd.mid, oid);

            // c.打包委托：orderCmd -> 网关模版数据commonMsg -> TCP数据流
            // d.发送数据
            gatewayConnection.sendOrder(orderCmd);

            log.info(orderCmd);
            return true;
        }

    }
}
