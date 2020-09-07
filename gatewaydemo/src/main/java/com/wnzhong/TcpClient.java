package com.wnzhong;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.log4j.Log4j2;

/**
 * 1. 断线能主动重连
 * 2. 按照规定的格式发送TCP数据
 */
@Log4j2
public class TcpClient {

    public static void main(String[] args) {
        new TcpClient().startConn();
    }

    private Vertx vertx;

    public void startConn() {
        vertx = Vertx.vertx();
        vertx.createNetClient().connect(8091, "127.0.0.1", new ClientConnHandler());
    }

    private class ClientConnHandler implements Handler<AsyncResult<NetSocket>> {
        @Override
        public void handle(AsyncResult<NetSocket> netSocketAsyncResult) {
            if (netSocketAsyncResult.succeeded()) {
                NetSocket netSocket = netSocketAsyncResult.result();

                // 关闭连接处理器
                netSocket.closeHandler(close -> {
                    log.info("Connect closed: ", netSocket.remoteAddress());
                    reconnect();
                });

                // 异常处理器
                netSocket.exceptionHandler(ex -> {
                    log.error("Error occurred.", ex);
                });

                // 发送消息
                byte[] req = "This message is send from client".getBytes();
                int bodyLength = req.length;
                Buffer buffer = Buffer.buffer().appendInt(bodyLength).appendBytes(req);
                netSocket.write(buffer);
            } else {
                log.error("Connect to 127.0.0.1:8091 failed.");
                reconnect();
            }
        }

        private void reconnect() {
            vertx.setTimer(1000 * 5, res -> {
                log.info("Re-connecting..");
                vertx.createNetClient().connect(8091, "127.0.0.1", new ClientConnHandler());
            });
        }
    }
}
