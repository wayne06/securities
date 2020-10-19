package com.wnzhong.engine.bean;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.util.Bits;
import com.wnzhong.engine.core.EngineApi;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CmdPack;
import thirdpart.codec.BodyCodec;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CmdPacketQueue {

    private static CmdPacketQueue instance = new CmdPacketQueue();

    private CmdPacketQueue() {}

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
    private BodyCodec bodyCodec;
    private EngineApi engineApi;

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
                } catch (InterruptedException e) {
                    log.error("Msg packet recvCache error, continue...", e);
                }
            }
        }).start();
    }

    private long lastPackNo = -1;

    private void handle(CmdPack cmdPack) {
        log.info("Receive: {}", cmdPack);

        // NACK
        long packNo = cmdPack.getPackNo();
        if (packNo == lastPackNo + 1) {

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
        }
    }

}
