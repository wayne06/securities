package com.wnzhong.gateway.bean;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.wnzhong.gateway.handler.ConnHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import thirdpart.checksum.CheckSum;
import thirdpart.codec.BodyCodec;
import thirdpart.fetchserv.FetchService;

@Getter
@Log4j2
public class GatewayConfig {

    /**
     * 网关 id
     */
    private short id;

    /**
     * 外网端口
     */
    private int recvPort;

    /**
     * 排队机 provider 端口
     */
    private int fetchServPort;

    //TODO 柜台列表，数据库连接

    @Setter
    private BodyCodec bodyCodec;

    @Setter
    private CheckSum checkSum;

    private Vertx vertx = Vertx.vertx();

    public void initConfig(String fileName) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(fileName);
        Element rootElement = document.getRootElement();
        id = Short.parseShort(rootElement.element("id").getText());
        recvPort = Integer.parseInt(rootElement.element("recvport").getText());
        fetchServPort = Integer.parseInt(rootElement.element("fetchservport").getText());
        log.info("Gateway ID: {}, PORT: {}, FetchServPort: {}", id, recvPort, fetchServPort);

        //TODO 柜台列表，数据库连接
    }

    public void startup() {
        // 1. 启动 TCP 服务监听
        initRecv();
        // TODO 2. 排队机交互
        initFetchServ();
    }

    private void initFetchServ() {
        ServerConfig rpcConfig = new ServerConfig()
                .setPort(fetchServPort)
                .setProtocol("bolt");
        ProviderConfig<FetchService> providerConfig = new ProviderConfig<FetchService>()
                .setInterfaceId(FetchService.class.getName())
                .setRef(() -> OrderCmdContainer.getInstance().getAll())
                .setServer(rpcConfig);
        providerConfig.export();
        log.info("Gateway startup fetchServ successfully at port: {}", fetchServPort);
    }

    private void initRecv() {
        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(new ConnHandler(this));
        netServer.listen(recvPort, res -> {
            if (res.succeeded()) {
                log.info("Gateway startup at port: {}", recvPort);
            } else {
                log.error("Gateway startup failed");
            }
        });
    }

}
