package com.wnzhong.engine.bean;

import com.alipay.remoting.exception.CodecException;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.util.Bits;
import com.google.common.collect.Lists;
import com.wnzhong.engine.core.EngineApi;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import thirdpart.bean.CmdPack;
import thirdpart.codec.BodyCodec;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CmdPacketQueue {

    private static CmdPacketQueue instance = new CmdPacketQueue();

    private CmdPacketQueue() {
    }

    public static CmdPacketQueue getInstance() {
        return instance;
    }

    /////////////////////////////////////////////////////////////////////////////

    private final BlockingQueue<CmdPack> recvCache = new LinkedBlockingDeque<>();

    public void cache(CmdPack cmdPack) {
        recvCache.offer(cmdPack);
    }

    /////////////////////////////////////////////////////////////////////////////

    private RheaKVStore orderKVStore;
    private BodyCodec   bodyCodec;
    private EngineApi   engineApi;

    public void init(RheaKVStore rheaKVStore, BodyCodec bodyCodec, EngineApi engineApi) {
        this.orderKVStore = rheaKVStore;
        this.bodyCodec = bodyCodec;
        this.engineApi = engineApi;

        new Thread(() -> {
            while (true) {
                try {
                    CmdPack cmdPack = recvCache.poll(10, TimeUnit.SECONDS);
                    if (cmdPack != null) {
                        handle(cmdPack);
                    }
                } catch (InterruptedException | CodecException e) {
                    log.error("Msg packet recvCache error, continue...", e);
                }
            }
        }).start();
    }

    private long lastPackNo = -1;

    private void handle(CmdPack cmdPack) throws CodecException {
        log.info("------------------new pack coming------------------------");
        log.info("Receive: {}", cmdPack);

        // NACK
        long packNo = cmdPack.getPackNo();
        log.info("packNo: {}", packNo);
        log.info("lastPackNo: {}", lastPackNo);
        if (packNo == lastPackNo + 1) {
            if (CollectionUtils.isEmpty(cmdPack.getOrderCmds())) {
                return;
            }
            for (OrderCmd cmd : cmdPack.getOrderCmds()) {
                engineApi.submitCommand(cmd);
            }
        } else if (packNo <= lastPackNo) {
            // 异常1：来自历史重复的包
            log.warn("Received duplicated packId: {}", packNo);
        } else {
            // 异常2：跳号
            log.info("The packNo lost from {} to {}", lastPackNo + 1, packNo);
            log.info("Retry querying from sequencer...");

            byte[] firstKey = new byte[8];
            Bits.putLong(firstKey, 0, lastPackNo + 1);

            byte[] lastKey = new byte[8];
            Bits.putLong(lastKey, 0, packNo + 1);

            final List<KVEntry> kvEntries = orderKVStore.bScan(firstKey, lastKey);
            if (CollectionUtils.isNotEmpty(kvEntries)) {
                List<CmdPack> collect = Lists.newArrayList();
                for (KVEntry entry : kvEntries) {
                    byte[] value = entry.getValue();
                    if (ArrayUtils.isNotEmpty(value)) {
                        collect.add(bodyCodec.deserialize(value, CmdPack.class));
                    }
                }
                collect.sort((o1, o2) -> (int) (o1.getPackNo() - o2.getPackNo()));
                for (CmdPack pack : collect) {
                    if (CollectionUtils.isEmpty(pack.getOrderCmds())) {
                        continue;
                    }
                    for (OrderCmd cmd : pack.getOrderCmds()) {
                        engineApi.submitCommand(cmd);
                    }
                }
            }
            // 排队机出错，导致出现了跳号
            lastPackNo = packNo;
        }
        log.info("packNo: {}", packNo);
        log.info("lastPackNo: {}", lastPackNo);
    }

}
