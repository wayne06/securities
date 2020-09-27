package com.wnzhong.seq.config;

import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MemoryDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import com.wnzhong.seq.bean.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Properties;

@Log4j2
@ToString
@RequiredArgsConstructor
public class SeqConfig {

    /**
     * 存 Raft 的数据在本地文件的路径
     */
    private String dataPath;

    /**
     * 当前节点提供服务的 ip 和 port
     */
    private String serverUrl;

    /**
     * 当前的 KVStore 集群由哪些节点构成
     */
    private String serverList;

    /**
     * 配置文件的文件名
     */
    @NonNull
    private String fileName;

    @Getter
    private Node node;

    public void startup() throws IOException {
        // 1.读取配置文件
        initConfig();
        // 2.初始化 KVStore 集群
        startSeqDbCluster();
        // todo 3.启动下游广播

        // todo 4.初始化网关连接
        startupFetch();
    }

    private void startupFetch() {

    }

    private void startSeqDbCluster() {
        final PlacementDriverOptions placementDriverOptions = PlacementDriverOptionsConfigured
                .newConfigured()
                .withFake(true)
                .config();
        final StoreEngineOptions storeEngineOptions = StoreEngineOptionsConfigured
                .newConfigured()
                .withStorageType(StorageType.Memory)
                .withMemoryDBOptions(MemoryDBOptionsConfigured.newConfigured().config())
                .withRaftDataPath(dataPath)
                .withServerAddress(new Endpoint(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1])))
                .config();
        final RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured
                .newConfigured()
                .withInitialServerList(serverList)
                .withStoreEngineOptions(storeEngineOptions)
                .withPlacementDriverOptions(placementDriverOptions)
                .config();
        node = new Node(rheaKVStoreOptions);
        node.start();
        // 把 node 的 stop 方法挂到系统的 shutdown 上面，即把 KVStore 节点的停止挂到 jdk shutdown 的流程中
        // 如果没有这一步的显示通知，很有可能集群会发生意料之外的事情
        Runtime.getRuntime().addShutdownHook(new Thread(node::stop));
        log.info("Start seq node successfully on port : {}", serverUrl.split(":")[1]);
    }

    private void initConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(Object.class.getResourceAsStream("/" + fileName));

        dataPath = properties.getProperty("datapath");
        serverUrl = properties.getProperty("serverurl");
        serverList = properties.getProperty("serverlist");

        log.info("Read config: {}", this);
    }


}
