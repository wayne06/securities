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
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.listener.ChannelListener;
import com.alipay.sofa.rpc.transport.AbstractChannel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wnzhong.seq.bean.FetchTask;
import com.wnzhong.seq.bean.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import thirdpart.codec.BodyCodec;
import thirdpart.fetchserv.FetchService;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

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
     * 下游网关地址
     */
    private String fetchUrls;

    /**
     * 启动排队机
     * @throws IOException
     */
    public void startup() throws IOException {
        // 1.读取配置文件
        initConfig();
        // 2.初始化 KVStore 集群
        startSeqDbCluster();
        // todo 3.启动下游广播

        // todo 4.初始化网关连接
        startupFetch();
    }

    ////////////////////////////////////////////////////抓取逻辑//////////////////////////////////////////////////////////

    /**
     * 保存所有到网关的连接（不是在配置项中读取，打印时排除）
     */
    @ToString.Exclude
    @Getter
    private Map<String, FetchService> fetchServiceMap = Maps.newConcurrentMap();

    /**
     * 解包功能（打包数据时使用）
     */
    @NonNull
    @ToString.Exclude
    @Getter
    private BodyCodec bodyCodec;

    /**
     * 1. 从哪些网关抓取：将下游网关地址定义在配置文件中
     * 2. 通信格式：采用rpc通讯方式，需要上下游有一个定义的接口，在thirdpart中
     */
    private void startupFetch() {
        // 1.建立所有到网关的连接
        for (String fetchUrl : fetchUrls.split(";")) {
            ConsumerConfig<FetchService> consumerConfig = new ConsumerConfig<FetchService>()
                    .setInterfaceId(FetchService.class.getName())  // 通信接口
                    .setProtocol("bolt")                           // RPC通信协议
                    .setTimeout(5000)                              // 超时时间
                    .setDirectUrl(fetchUrl);                       // 直连地址
            consumerConfig.setOnConnect(Lists.newArrayList(new FetchChannelListener(consumerConfig)));  // 绑定监听器
            // 根据实际使用情况，客户端在第一次连上Provider的时候，onConnected()方法不会执行，只有重连时才会进入其中，所以要显示put
            fetchServiceMap.put(fetchUrl, consumerConfig.refer());
        }
        // 2.启动定时任务，到每个网关收取数据
        new Timer().schedule(new FetchTask(this), 5000, 1000);


    }

    @RequiredArgsConstructor
    private class FetchChannelListener implements ChannelListener {

        @NonNull
        private ConsumerConfig<FetchService> consumerConfig;

        @Override
        public void onConnected(AbstractChannel channel) {
            String remoteAddr = channel.remoteAddress().toString();
            log.info("Connected to gateway : {}", remoteAddr);
            fetchServiceMap.put(remoteAddr, consumerConfig.refer());
        }

        @Override
        public void onDisconnected(AbstractChannel channel) {
            String remoteAddr = channel.remoteAddress().toString();
            log.info("Disconnected to gateway : {}", remoteAddr);
            fetchServiceMap.remove(remoteAddr);
        }
    }

    ////////////////////////////////////////////////////启动DB//////////////////////////////////////////////////////////

    /**
     * KVStore节点
     */
    @Getter
    private Node node;

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

    ////////////////////////////////////////////////////读取配置//////////////////////////////////////////////////////////

    /**
     * 配置文件的文件名
     */
    @NonNull
    private String fileName;

    private void initConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(Object.class.getResourceAsStream("/" + fileName));

        dataPath = properties.getProperty("datapath");
        serverUrl = properties.getProperty("serverurl");
        serverList = properties.getProperty("serverlist");
        fetchUrls = properties.getProperty("fetchurls");

        log.info("Read config: {}", this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////


}
