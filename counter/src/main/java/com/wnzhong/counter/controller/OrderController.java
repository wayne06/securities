package com.wnzhong.counter.controller;

import com.wnzhong.counter.bean.res.CounterRes;
import com.wnzhong.counter.cache.StockCache;
import com.wnzhong.counter.config.CounterConfig;
import com.wnzhong.counter.service.OrderService;
import com.wnzhong.counter.util.IDConverter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thirdpart.order.CmdType;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderDirection;
import thirdpart.order.OrderType;

@RestController
@RequestMapping("/api")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockCache stockCache;

    @Autowired
    private CounterConfig counterConfig;

    @RequestMapping("/stock")
    public CounterRes getStock(@RequestParam String keyword) {
        return new CounterRes(stockCache.getStocks(keyword));
    }

    @RequestMapping("/balance")
    public CounterRes getBalance(@RequestParam long uid) {
        return new CounterRes(orderService.getBalance(uid));
    }

    @RequestMapping("/posi")
    public CounterRes getPosi(@RequestParam long uid) {
        return new CounterRes(orderService.getPosi(uid));
    }

    @RequestMapping("/order")
    public CounterRes getOrder(@RequestParam long uid) {
        return new CounterRes(orderService.getOrder(uid));
    }

    @RequestMapping("/trade")
    public CounterRes getTrade(@RequestParam long uid) {
        return new CounterRes(orderService.getTrade(uid));
    }

    @RequestMapping("/sendorder")
    public CounterRes order(@RequestParam int uid, @RequestParam short type, @RequestParam long timestamp,
                            @RequestParam int code, @RequestParam byte direction, @RequestParam long price,
                            @RequestParam long volume, @RequestParam byte ordertype) {
        OrderCmd orderCmd = OrderCmd.builder()
                                    .uid(uid)
                                    .mid(counterConfig.getId())
                                    .type(CmdType.of(type))
                                    .timestamp(timestamp)
                                    .code(code)
                                    .direction(OrderDirection.of(direction))
                                    .price(price)
                                    .volume(volume)
                                    .orderType(OrderType.of(ordertype))
                                    .build();
        if (orderService.sendOrder(orderCmd, counterConfig)) {
            return new CounterRes(CounterRes.SUCCESS, "SAVE ORDER SUCCESS", null);
        } else {
            return new CounterRes(CounterRes.FAIL, "SAVE ORDER FAILED", null);
        }
    }

    @RequestMapping("/cancelorder")
    public CounterRes cancelOrder(@RequestParam int uid, @RequestParam int counterOId, @RequestParam int code) {
        OrderCmd orderCmd = OrderCmd.builder()
                                    .uid(uid)
                                    .code(code)
                                    .type(CmdType.CANCEL_ORDER)
                                    .oid(IDConverter.combineInt2Long(counterConfig.getId(), counterOId))
                                    .build();
        if (orderService.cancelOrder(orderCmd)) {
            return new CounterRes(CounterRes.SUCCESS, "CANCEL ORDER SUCCESS", null);
        } else {
            return new CounterRes(CounterRes.FAIL, "CANCEL ORDER FAILED", null);
        }
    }

}
