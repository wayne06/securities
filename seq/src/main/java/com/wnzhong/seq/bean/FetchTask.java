package com.wnzhong.seq.bean;

import com.google.common.collect.Lists;
import com.wnzhong.seq.config.SeqConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import thirdpart.fetchserv.FetchService;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * 从 map 中拿到所有网关的链接，遍历链接，并从网关中逐一捞取数据，获取所有数据后再定序
 *
 * @author wayne
 */
@Log4j2
@RequiredArgsConstructor
public class FetchTask extends TimerTask {

    @NonNull
    private SeqConfig seqConfig;

    @Override
    public void run() {
        // 1.遍历主节点（并不是所有节点都要查取数据，只需查主节点）
        if (!seqConfig.getNode().isLeader()) {
            return;
        }
        Map<String, FetchService> fetchServiceMap = seqConfig.getFetchServiceMap();
        if (MapUtils.isEmpty(fetchServiceMap)) {
            return;
        }

        // 2.获取数据（map中的所有 order 数据）
        List<OrderCmd> cmds = collectAllOrders(fetchServiceMap);
        if (CollectionUtils.isEmpty(cmds)) {
            return;
        }
        log.info(cmds);

        // 3.对数据进行排序：时间优先；价格优先；量优先
        cmds.sort((o1, o2) -> {
            int res = compareTime(o1, o2);
            if (res != 0) {
                return res;
            }
            res = comparePrice(o1, o2);
            if (res != 0) {
                return res;
            }
            res = compareVolume(o1, o2);
            return res;
        });

    }

    private int compareVolume(OrderCmd o1, OrderCmd o2) {
        return 0;
    }

    private int comparePrice(OrderCmd o1, OrderCmd o2) {
        return 0;
    }

    private int compareTime(OrderCmd o1, OrderCmd o2) {
        return 0;
    }

    private List<OrderCmd> collectAllOrders(Map<String, FetchService> fetchServiceMap) {
        //这种写法存在的问题：性能不可控，调试不方便，可读性差
        //List<OrderCmd> res = fetchServiceMap.values().stream()
        //        .map(t -> t.fetchData())
        //        .filter(msg -> CollectionUtils.isEmpty(msg))
        //        .flatMap(List::stream)
        //        .collect(Collectors.toList());

        List<OrderCmd> res = Lists.newArrayList();
        fetchServiceMap.values().forEach(t -> {
            List<OrderCmd> orderCmds = t.fetchData();
            if (CollectionUtils.isNotEmpty(orderCmds)) {
                res.addAll(orderCmds);
            }
        });
        return res;
    }
}
