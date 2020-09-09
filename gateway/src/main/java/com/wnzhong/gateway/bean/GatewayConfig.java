package com.wnzhong.gateway.bean;

import com.wnzhong.gateway.bean.handler.ConnHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import thirdpart.BodyCodec;
import thirdpart.checksum.CheckSum;

@Getter
@Log4j2
public class GatewayConfig {

    private short id;

    private int recvPort;

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
        recvPort = Integer.parseInt(rootElement.element("recvPort").getText());
        log.info("Gateway ID: {}, PORT: {}", id, recvPort);
    }

    public void startup() {
        // 1. 启动 TCP 服务监听
        initRecv();
        // TODO 2. 排队机交互
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
