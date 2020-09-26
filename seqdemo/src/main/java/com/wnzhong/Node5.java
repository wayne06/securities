package com.wnzhong;

import com.alipay.sofa.jraft.rhea.LeaderStateListener;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MemoryDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicLong;

@Log4j2
public class Node5 {

    private static final AtomicLong leaderTerm = new AtomicLong(-1);

    public static void main(String[] args) {

        String ip = "127.0.0.1";
        int port = 8895;

        String dataPath = "E:\\test\\jraftTestServer\\server5";

        String serverList = "127.0.0.1:8891," +
                            "127.0.0.1:8892," +
                            "127.0.0.1:8893," +
                            "127.0.0.1:8894," +
                            "127.0.0.1:8895";

        final StoreEngineOptions storeEngineOptions = StoreEngineOptionsConfigured
                .newConfigured()
                .withStorageType(StorageType.Memory)
                .withMemoryDBOptions(MemoryDBOptionsConfigured.newConfigured().config())
                .withRaftDataPath(dataPath)
                .withServerAddress(new Endpoint(ip, port))
                .config();

        final PlacementDriverOptions placementDriverOptions = PlacementDriverOptionsConfigured
                .newConfigured()
                .withFake(true)
                .config();

        final RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured
                .newConfigured()
                .withInitialServerList(serverList)
                .withStoreEngineOptions(storeEngineOptions)
                .withPlacementDriverOptions(placementDriverOptions)
                .config();

        RheaKVStore rheaKVStore = new DefaultRheaKVStore();
        rheaKVStore.init(rheaKVStoreOptions);

        rheaKVStore.addLeaderStateListener(-1, new LeaderStateListener() {

            @Override
            public void onLeaderStart(long newTerm) {
                log.info("Node turned to be leader, newTerm = {}", newTerm);
            }

            @Override
            public void onLeaderStop(long oldTerm) {
                leaderTerm.set(-1);
            }
        });

    }

}
