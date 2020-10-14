package com.wnzhong.engine.bean;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.wnzhong.engine.core.EngineApi;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CmdPack;
import thirdpart.codec.BodyCodec;

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
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handle(CmdPack cmdPack) {

    }

}
