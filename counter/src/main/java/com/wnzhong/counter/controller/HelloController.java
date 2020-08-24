package com.wnzhong.counter.controller;

import com.wnzhong.counter.util.DbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/http")
    public String testHttp() {
        return "hello http.";
    }

    @RequestMapping("/db")
    public String testDb() {
        return "" + DbUtil.getId();
    }

    @RequestMapping("/redis")
    public String testRedis() {
        redisTemplate.opsForValue().set("test:Hello", "Redis");
        return redisTemplate.opsForValue().get("test:Hello");
    }

}
