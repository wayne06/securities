package com.wnzhong.counter.config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import thirdparty.checksum.CheckSum;
import thirdparty.codec.BodyCodec;

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

    //////////////////////////////编码相关配置///////////////////////////////////

    @Value("${counter.checksum}")
    private String checkSumClass;

    @Value("${counter.bodycodec}")
    private String bodyCodecClass;

    private CheckSum checksum;

    private BodyCodec bodyCodec;

    @PostConstruct
    private void init() {
        Class<?> clazz;
        try {
            clazz = Class.forName(checkSumClass);
            checksum = (CheckSum) clazz.getDeclaredConstructor().newInstance();
            clazz = Class.forName(bodyCodecClass);
            bodyCodec = (BodyCodec) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Counter config init error: ", e);
        }
    }


}
