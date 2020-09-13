package com.wnzhong.gateway.handler;

import com.wnzhong.gateway.bean.GatewayConfig;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;

@RequiredArgsConstructor
@Log4j2
public class ConnHandler implements Handler<NetSocket> {

    @NonNull
    private GatewayConfig gatewayConfig;

    /**
     * 包头[ 包体长度 int + 校验和 byte + src short+ dst short + 消息类型 short + 消息状态 byte + 包编号 long ]
     */
    private static final int PACKET_HEADER_LENGTH = 4 + 1 + 2 + 2 + 2 + 1 + 8;

    @Override
    public void handle(NetSocket netSocket) {

        MsgHandler msgHandler = new MsgHandlerImpl(gatewayConfig.getBodyCodec());
        msgHandler.onConnect(netSocket);

        // 1. 解析
        final RecordParser parser = RecordParser.newFixed(PACKET_HEADER_LENGTH);
        parser.setOutput(new Handler<Buffer>() {
            int bodyLength = -1;
            byte checkSum = -1;
            short msgSrc = -1;
            short msgDst = -1;
            short msgType = -1;
            byte status = -1;
            long packetNo = -1;
            @Override
            public void handle(Buffer buffer) {
                if (bodyLength == -1) {
                    // a. 读取包头
                    bodyLength = buffer.getInt(0);
                    checkSum = buffer.getByte(4);
                    msgSrc = buffer.getShort(5);
                    msgDst = buffer.getShort(7);
                    msgType = buffer.getShort(9);
                    status = buffer.getByte(11);
                    packetNo = buffer.getLong(12);
                    parser.fixedSizeMode(bodyLength);
                } else {
                    // b. 读取数据
                    byte[] bodyBytes = buffer.getBytes();
                    // c. 组装对象
                    CommonMsg commonMsg;
                    log.info("checksum in msg: " + checkSum);
                    log.info("checksum by calculating : " + gatewayConfig.getCheckSum().getSum(bodyBytes));
                    if (checkSum != gatewayConfig.getCheckSum().getSum(bodyBytes)) {
                        log.error("Illegal byte body from client: {}", netSocket.remoteAddress());
                        return;
                    } else {
                        if (msgDst != gatewayConfig.getId()) {
                            log.error("Incorrect msgDst: {} from client: {}", msgDst, netSocket.remoteAddress());
                            return;
                        }
                        commonMsg = new CommonMsg();
                        commonMsg.setBodyLength(bodyBytes.length);
                        commonMsg.setChecksum(checkSum);
                        commonMsg.setMsgSrc(msgSrc);
                        commonMsg.setMsgDst(msgDst);
                        commonMsg.setMsgType(msgType);
                        commonMsg.setStatus(status);
                        commonMsg.setMsgNo(packetNo);
                        commonMsg.setBody(bodyBytes);
                        commonMsg.setTimestamp(System.currentTimeMillis());

                        log.info(commonMsg);

                        msgHandler.onCounterData(commonMsg);

                        // d. 恢复现场
                        bodyLength = -1;
                        checkSum = -1;
                        msgSrc = -1;
                        msgDst = -1;
                        msgType = -1;
                        status = -1;
                        packetNo = -1;
                        parser.fixedSizeMode(PACKET_HEADER_LENGTH);
                    }

                }
            }
        });
        netSocket.handler(parser);

        // 2. 异常、退出
        netSocket.closeHandler(close -> {
            msgHandler.onDisconnect(netSocket);
        });
        netSocket.exceptionHandler(ex -> {
            msgHandler.onException(netSocket, ex);
        });

    }
}
