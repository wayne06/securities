package com.wnzhong.seq.bean;

import com.alipay.sofa.jraft.util.Bits;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.google.common.collect.Lists;
import com.wnzhong.seq.config.SeqConfig;
import io.vertx.core.buffer.Buffer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import thirdpart.bean.CmdPack;
import thirdpart.fetchserv.FetchService;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderDirection;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;

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
            // 写法一（hard code: 重复，冗余，可读性差）
            //if (o1.timestamp > o2.timestamp) return 1;
            //else if (o1.timestamp < o2.timestamp) return -1;
            //else {
            //    if (o1.direction == OrderDirection.BUY) {
            //        if (o1.direction == o2.direction) {
            //            if (o1.price > o2.price) return -1;
            //            else if (o1.price < o2.price) return 1;
            //            else return 0;
            //        } else {
            //            return 0;
            //        }
            //    } else if (o1.direction == OrderDirection.SELL) {
            //        if (o1.direction == o2.direction) {
            //            if (o1.price < o2.price) return -1;
            //            else if (o1.price > o2.price) return 1;
            //            else return 0;
            //        } else {
            //            return 0;
            //        }
            //    } else {
            //        return 0;
            //    }
            //}

            // 写法二
            int res = compareByTime(o1, o2);
            if (res != 0) {
                return res;
            }
            res = compareByPrice(o1, o2);
            if (res != 0) {
                return res;
            }
            res = compareByVolume(o1, o2);
            return res;
        });

        // TODO 4.存储到 KVStore，发送到撮合核心
        try {
            // 1.生成PacketNo(与下游约定的校验标准)
            long packetNo = getPacketNoFromStore();

            // 2.入库
            CmdPack pack = new CmdPack(packetNo, cmds);
            byte[] serialize = seqConfig.getBodyCodec().serialize(pack);
            insertToKVStore(packetNo, serialize);

            // 3.更新PacketNo+1
            updatePacketNoInStore(packetNo + 1);

            // 4.发送
            seqConfig.getMulticastSender().send(
                    Buffer.buffer(serialize),
                    seqConfig.getMulticastPort(),
                    seqConfig.getMulticastIp(),
                    null
            );

        } catch (Exception e) {
            log.error("Encode cmd packet error.", e);
        }
    }

    /**
     * 更新packetNo
     * @param packetNo
     */
    private void updatePacketNoInStore(long packetNo) {
        final byte[] bytes = new byte[8];
        Bits.putLong(bytes, 0, packetNo);
        seqConfig.getNode().getRheaKVStore().put(PACKET_NO_KEY, bytes);
    }

    /**
     * 保存数据到KVStore
     * @param packetNo
     * @param serialize
     */
    private void insertToKVStore(long packetNo, byte[] serialize) {
        byte[] key = new byte[8];
        Bits.putLong(key, 0, packetNo);

        // put是异步操作，bPut是同步操作
        seqConfig.getNode().getRheaKVStore().put(key, serialize);
    }

    private static final byte[] PACKET_NO_KEY = BytesUtil.writeUtf8("seq_packet_no");

    /**
     * 获取PacketNo
     * @return
     */
    private long getPacketNoFromStore() {
        final byte[] bPacketNo = seqConfig.getNode().getRheaKVStore().bGet(PACKET_NO_KEY);
        long packetNo = 0;
        if (ArrayUtils.isNotEmpty(bPacketNo)) {
            packetNo = Bits.getLong(bPacketNo, 0);
        }
        return packetNo;
    }

    private int compareByVolume(OrderCmd o1, OrderCmd o2) {
        if (o1.volume > o2.volume) return -1;
        else if (o1.volume < o2.volume) return 1;
        return 0;
    }

    private int compareByPrice(OrderCmd o1, OrderCmd o2) {
        if (o1.direction == o2.direction) {
            if (o1.price > o2.price) return o1.direction == OrderDirection.BUY ? -1 : 1;
            else if (o1.price < o2.price) return o1.direction == OrderDirection.BUY ? 1 : -1;
            else return 0;
        }
        return 0;
    }

    private int compareByTime(OrderCmd o1, OrderCmd o2) {
        if (o1.timestamp > o2.timestamp) return 1;
        else if (o2.timestamp < o2.timestamp) return -1;
        return 0;
    }

    private List<OrderCmd> collectAllOrders(Map<String, FetchService> fetchServiceMap) {
        //写法一（存在的问题：性能不可控，调试不方便，可读性差）
        //List<OrderCmd> res = fetchServiceMap.values().stream()
        //        .map(t -> t.fetchData())
        //        .filter(msg -> CollectionUtils.isEmpty(msg))
        //        .flatMap(List::stream)
        //        .collect(Collectors.toList());

        //写法二
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
