package thirdpart.tcp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Log4j2
@RequiredArgsConstructor
public class TcpDirectSender {

    @NonNull
    private String ip;

    @NonNull
    private int port;

    @NonNull
    private Vertx vertx;

    /**
     * 用 volatile 修饰，是为了每次在取 netSocket 的时候，一定是取的内存中最新的那一份
     * 因为网络编程中有这样一种问题，发数据时可能产生重连，如果重连，netSocket肯定会重置，为了拿到最新的 socket，用 volatile 修饰
     */
    private volatile NetSocket netSocket;

    /**
     * 如果每来一次数据就用 socket.write，那么会造成：
     * 1. 无法做流量控制
     * 2. 数据有可能会阻塞在网卡里，这种阻塞 socket 不会告知外部，若阻塞太多还会导致部分包丢失
     * 解决方式：在收到委托和发送委托之间增加缓存，让 socket去缓存中取数据
     */
    private final BlockingQueue<Buffer> dataCache = new LinkedBlockingDeque<>();

    /**
     * 该 send 方法是将数据流放入缓存，而真正的把报文发送到柜台的逻辑单独开一个线程来做
     * @param buffer
     * @return
     */
    public boolean send(Buffer buffer) {
        return dataCache.offer(buffer);
    }

    public void startup() {
        // 创建客户端连接
        vertx.createNetClient().connect(port, ip, new ClientConnHandler());

        // 真正的把报文发送到柜台的逻辑单独开一个线程来做
        new Thread(() -> {
            while (true) {
                try {
                    Buffer buffer = dataCache.poll(5, TimeUnit.SECONDS);
                    if (buffer != null && buffer.length() > 0 && netSocket != null) {
                        // 真正的发送报文代码
                        netSocket.write(buffer);
                    }
                } catch (InterruptedException e) {
                    log.error("Message sending failed..Continue", e.getCause());
                }
            }
        }).start();
    }

    private class ClientConnHandler implements Handler<AsyncResult<NetSocket>> {
        @Override
        public void handle(AsyncResult<NetSocket> netSocketAsyncResult) {
            if (netSocketAsyncResult.succeeded()) {
                log.info("Connected to Gateway - {} : {} ", ip, port);
                netSocket = netSocketAsyncResult.result();

                netSocket.closeHandler(close -> {
                    log.info("Connect closed: ", netSocket.remoteAddress());
                    reconnect();
                });

                netSocket.exceptionHandler(ex -> {
                    log.error("Error occurred.", ex.getCause());
                });

            } else {
                log.error("Connect to {}:{} failed.", ip, port);
                reconnect();
            }
        }

        private void reconnect() {
            vertx.setTimer(1000 * 5, res -> {
                log.info("Re-connecting..");
                vertx.createNetClient().connect(port, ip, new ClientConnHandler());
            });
        }
    }
}
