package com.wnzhong.counter.config;

import com.wnzhong.counter.bean.consumer.MqttBusConsumer;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import thirdpart.checksum.CheckSum;
import thirdpart.codec.BodyCodec;
import thirdpart.codec.MsgCodec;

import javax.annotation.PostConstruct;

@Getter
@Component
@Log4j2
public class CounterConfig {

    //////////////////////////////会员号///////////////////////////////////

    @Value("${counter.id}")
    private short id;

    //////////////////////////////UUID相关配置///////////////////////////////////

    @Value("${counter.dataCenterId}")
    private long dataCenterId;

    @Value("${counter.workerId}")
    private long workerId;

    //////////////////////////////网关相关配置///////////////////////////////////

    @Value("${counter.sendIp}")
    private String sendIp;

    @Value("${counter.sendPort}")
    private int sendPort;

    @Value(("${counter.gatewayId}"))
    private short gatewayId;

    private Vertx vertx = Vertx.vertx();

    //////////////////////////////websocket配置///////////////////////////////////

    @Value("${counter.pubport}")
    private int pubPort;

    //////////////////////////////总线配置///////////////////////////////////

    @Value("${counter.subbusip}")
    private String subBusIp;

    @Value("${counter.subbusport}")
    private int subBusPort;

    //////////////////////////////编码相关配置///////////////////////////////////

    @Value("${counter.checksum}")
    private String checkSumClass;

    @Value("${counter.bodycodec}")
    private String bodyCodecClass;

    @Value("${counter.msgcodec}")
    private String msgCodecClass;

    private CheckSum checksum;

    private BodyCodec bodyCodec;

    private MsgCodec msgCodec;

    @PostConstruct
    private void init() {
        Class<?> clazz;
        try {
            clazz = Class.forName(checkSumClass);
            checksum = (CheckSum) clazz.getDeclaredConstructor().newInstance();
            clazz = Class.forName(bodyCodecClass);
            bodyCodec = (BodyCodec) clazz.getDeclaredConstructor().newInstance();
            clazz = Class.forName(msgCodecClass);
            msgCodec = (MsgCodec) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Counter config init error: ", e);
        }

        //初始化总线连接
        new MqttBusConsumer(subBusIp, subBusPort, String.valueOf(id), msgCodec, checksum, vertx).startup();
    }


}
