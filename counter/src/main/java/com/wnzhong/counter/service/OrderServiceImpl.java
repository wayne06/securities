package com.wnzhong.counter.service;

import com.wnzhong.counter.bean.OrderInfo;
import com.wnzhong.counter.bean.PosiInfo;
import com.wnzhong.counter.bean.TradeInfo;
import com.wnzhong.counter.util.DbUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import thirdparty.order.OrderCmd;

import java.util.List;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

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
        int oid = DbUtil.saveOrder(orderCmd);
        if (oid < 0) {
            return false;
        } else {
            log.info(orderCmd);
            return true;
        }

    }
}
