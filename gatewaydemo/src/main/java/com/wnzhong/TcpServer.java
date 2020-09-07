package com.wnzhong;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TcpServer {

    public static void main(String[] args) {
        new TcpServer().startServer();
    }

    public void startServer() {
        Vertx vertx = Vertx.vertx();
        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(new ConnHandler());
        netServer.listen(8091, res -> {
            if (res.succeeded()) {
                log.info("Gateway startup at port 8091..");
            } else {
                log.info("Gateway start failed.");
            }
        });
    }

    private class ConnHandler implements Handler<NetSocket> {

        // 报文：报头(int,报文长度) + 报体(byte[],数据)

        private static final int PACKET_HEADER_LENGTH = 4;

        @Override
        public void handle(NetSocket netSocket) {
            // 自定义解析器
            final RecordParser parser = RecordParser.newFixed(PACKET_HEADER_LENGTH);
            parser.setOutput(new Handler<Buffer>() {

                int bodyLength = -1;

                @Override
                public void handle(Buffer buffer) {
                    if (bodyLength == -1) {
                        // 读取报头
                        bodyLength = buffer.getInt(0);
                        parser.fixedSizeMode(bodyLength);
                    } else {
                        // 读取数据
                        byte[] bodyBytes = buffer.getBytes();
                        log.info("Message from client: {}", new String(bodyBytes));

                        // 恢复现场
                        parser.fixedSizeMode(PACKET_HEADER_LENGTH);
                        bodyLength = -1;
                    }
                }
            });

            netSocket.handler(parser);
        }
    }
}
