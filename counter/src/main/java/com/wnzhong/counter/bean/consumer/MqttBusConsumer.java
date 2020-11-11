package com.wnzhong.counter.bean.consumer;

import com.google.common.collect.Maps;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import thirdpart.bean.CommonMsg;
import thirdpart.checksum.CheckSum;
import thirdpart.codec.MsgCodec;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static thirdpart.bean.MsgConstants.MATCH_HQ_DATA;
import static thirdpart.bean.MsgConstants.MATCH_ORDER_DATA;

/**
 * @author wayne
 */
@RequiredArgsConstructor
@Log4j2
public class MqttBusConsumer {

    @NonNull
    private String busIp;

    @NonNull
    private int busPort;

    @NonNull
    private String recvAddr;

    @NonNull
    private MsgCodec msgCodec;

    @NonNull
    private CheckSum checkSum;

    @NonNull
    private Vertx vertx;

    private final static String HQ_ADDR = "-1";

    public static final String INNER_MARKET_DATA_CACHE_ADDR = "l1_market_data_cache_addr";

    public static final String INNER_MATCH_DATA_ADDR = "match_data_addr";

    public void startup() {
        mqttConnect(vertx, busPort, busIp);
    }

    private void mqttConnect(Vertx vertx, int busPort, String busIp) {
        MqttClient mqttClient = MqttClient.create(vertx);
        mqttClient.connect(busPort, busIp, res -> {
            if (res.succeeded()) {
                log.info("Connect mqtt bus succeed.");
                Map<String, Integer> topic = Maps.newHashMap();
                topic.put(recvAddr, MqttQoS.AT_LEAST_ONCE.value());
                topic.put(HQ_ADDR, MqttQoS.AT_LEAST_ONCE.value());
                mqttClient.subscribe(topic).publishHandler(h -> {
                    CommonMsg msg = msgCodec.decodeFromBuffer(h.payload());
                    if (msg.getChecksum() != (checkSum.getSum(msg.getBody()))) {
                        return;
                    }
                    byte[] body = msg.getBody();
                    if (ArrayUtils.isNotEmpty(body)) {
                        short msgType = msg.getMsgType();
                        //去重
                        //long msgNo = msg.getMsgNo();
                        if (msgType == MATCH_ORDER_DATA) {
                            vertx.eventBus().send(INNER_MATCH_DATA_ADDR, Buffer.buffer(body));
                        } else if (msgType == MATCH_HQ_DATA) {
                            vertx.eventBus().send(INNER_MARKET_DATA_CACHE_ADDR, Buffer.buffer(body));
                        } else {
                            log.error("Recv unknown msgType: {}", msg);
                        }
                    }
                });
            } else {
                log.error("Connect mqtt bus failed.");
            }
        });
        mqttClient.closeHandler(h -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                log.error(e);
            }
            mqttConnect(vertx, busPort, busIp);
        });

    }

}
