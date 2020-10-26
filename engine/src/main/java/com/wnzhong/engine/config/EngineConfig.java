package com.wnzhong.engine.config;

import com.alipay.remoting.exception.CodecException;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.wnzhong.engine.bean.CmdPacketQueue;
import com.wnzhong.engine.core.EngineApi;
import com.wnzhong.engine.db.DbQuery;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.dbutils.QueryRunner;
import thirdpart.bean.CmdPack;
import thirdpart.checksum.CheckSum;
import thirdpart.codec.BodyCodec;
import thirdpart.codec.MsgCodec;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * @author wayne
 */
@Log4j2
@ToString
@Getter
@RequiredArgsConstructor
public class EngineConfig {

    private short  id;
    private String orderRecvIp;
    private int    orderRecvPort;
    private String seqUrlList;
    private String pubIp;
    private int    pubPort;

    @NonNull
    private String    fileName;
    @NonNull
    private BodyCodec bodyCodec;
    @NonNull
    private CheckSum  checkSum;
    @NonNull
    private MsgCodec  msgCodec;

    private Vertx vertx = Vertx.vertx();

    public void startup() throws IOException {
        //1.读取配置文件
        initConfig();

        //2.数据库连接
        initDB();

        //3.启动撮合核心
        startEngine();

        //4.建立总线连接，初始化数据的发送
        // （初始化服务时，总是先初始化对外发送数据的功能，再初始化跟上游的连接）
        initPub();

        //5.初始化接收排队机数据及连接
        startSeqConn();
    }

    ////////////////////////////////////////////连接排队机///////////////////////////////////////////////////

    @Getter
    @ToString.Exclude
    private final RheaKVStore orderKVStore = new DefaultRheaKVStore();

    @Getter
    private EngineApi engineApi = new EngineApi();

    private void startSeqConn() {
        //1.完成到KVStore的连接
        final List<RegionRouteTableOptions> regionRouteTableOptions = MultiRegionRouteTableOptionsConfigured
                .newConfigured()
                .withInitialServerList(-1L, seqUrlList)
                .config();
        PlacementDriverOptionsConfigured placementDriverOptionsConfigured = PlacementDriverOptionsConfigured
                .newConfigured();
        placementDriverOptionsConfigured.withFake(true);
        placementDriverOptionsConfigured.withRegionRouteTableOptionsList(regionRouteTableOptions);
        final PlacementDriverOptions placementDriverOptions = placementDriverOptionsConfigured
                .config();
        final RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured
                .newConfigured()
                .withPlacementDriverOptions(placementDriverOptions)
                .config();
        orderKVStore.init(rheaKVStoreOptions);

        //2.在组播地址上做监听，用来接收发过来的委托数据流（放在缓存队列中，由另一个线程从队列中拿数据进行处理）

        //2.1 委托指令处理器（接收来自排队机的数据）
        CmdPacketQueue.getInstance().init(orderKVStore, bodyCodec, engineApi);
        //2.2 组播方式（好处是：允许多个socket接收同一份数据）
        DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());
        socket.listen(
                orderRecvPort,
                "0.0.0.0",
                asyncRes -> {
                    if (asyncRes.succeeded()) {

                        socket.handler(packet -> {
                            Buffer udpData = packet.data();
                            if (udpData.length() > 0) {
                                try {
                                    CmdPack cmdPack = bodyCodec.deserialize(udpData.getBytes(), CmdPack.class);
                                    CmdPacketQueue.getInstance().cache(cmdPack);
                                } catch (CodecException e) {
                                    log.error("Decode packet error", e);
                                }
                            } else {
                                log.error("Receive empty udp packet from client {}", packet.sender().toString());
                            }
                        });


                        try {
                            socket.listenMulticastGroup(
                                    orderRecvIp,
                                    mainInterface().getName(),
                                    null,
                                    asyncRes2 -> log.info("Listen succeed: {}", asyncRes2.succeeded()));
                        } catch (Exception e) {
                            log.error(e);
                        }
                    } else {
                        log.error("Listen failed,", asyncRes.cause());
                    }
                });
    }

    /**
     * 获取合适的网卡（适合接收UDP数据的网卡需要满足：1.非loopback网卡 2.支持multicast 3.非虚拟机网卡 4.有IPV4）
     * @return
     */
    private static NetworkInterface mainInterface() throws SocketException {
        final ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        final NetworkInterface networkInterface =
                interfaces.stream()
                        .filter(t -> {
                            try {
                                final boolean isLoopback = t.isLoopback();
                                final boolean supportMulticast = t.supportsMulticast();
                                final boolean isVirtualBox = t.getDisplayName().contains("VirtualBox")
                                        || t.getDisplayName().contains("Host-only");
                                final boolean hasIpv4 = t.getInterfaceAddresses().stream()
                                        .anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
                                return !isLoopback && supportMulticast && !isVirtualBox && hasIpv4;
                            } catch (SocketException e) {
                                log.error("Find network interface error.", e);
                            }
                            return false;
                        })
                        .sorted(Comparator.comparing(NetworkInterface::getName))
                        .findFirst()
                        .orElse(null);
        return networkInterface;
    }

    ///////////////////////////////////////////建立总线连接//////////////////////////////////////////////////

    private void initPub() {

    }

    ///////////////////////////////////////////启动撮合核心//////////////////////////////////////////////////

    private void startEngine() {

    }

    ////////////////////////////////////////////数据库连接///////////////////////////////////////////////////

    @Getter
    private DbQuery dbQuery;

    private void initDB() {
        QueryRunner queryRunner = new QueryRunner(new ComboPooledDataSource());
        dbQuery = new DbQuery(queryRunner);
    }

    /////////////////////////////////////////////读取配置///////////////////////////////////////////////////

    private void initConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/" + fileName));
        id = Short.parseShort(properties.getProperty("id"));
        orderRecvIp = properties.getProperty("orderrecvip");
        orderRecvPort = Integer.parseInt(properties.getProperty("orderrecvport"));
        seqUrlList = properties.getProperty("sequrllist");
        pubIp = properties.getProperty("pubip");
        pubPort = Integer.parseInt(properties.getProperty("pubport"));
        log.info(this);
    }

}
