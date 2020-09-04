package com.wnzhong.counter;

import com.wnzhong.counter.config.CounterConfig;
import com.wnzhong.counter.util.DbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import thirdparty.uuid.MyUuid;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CounterApplication {

    @Autowired
    private DbUtil dbUtil;

    @Autowired
    private CounterConfig counterConfig;

    @PostConstruct
    private void init() {
        MyUuid.getInstance().init(counterConfig.getDataCenterId(), counterConfig.getWorkerId());
    }

    public static void main(String[] args) {
        SpringApplication.run(CounterApplication.class, args);
    }

}
