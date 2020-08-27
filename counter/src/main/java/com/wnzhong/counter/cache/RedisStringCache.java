package com.wnzhong.counter.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class RedisStringCache {

    private static RedisStringCache redisStringCache;

    private RedisStringCache(){}

    @Autowired
    private StringRedisTemplate template;

    @Value("${cacheexpire.captcha}")
    private int captchaExpireTime;

    @Value("${cacheexpire.account}")
    private int accountExpireTime;

    @Value("${cacheexpire.order}")
    private int orderExpireTime;

    public StringRedisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(StringRedisTemplate template) {
        this.template = template;
    }

    public int getCaptchaExpireTime() {
        return captchaExpireTime;
    }

    public void setCaptchaExpireTime(int captchaExpireTime) {
        this.captchaExpireTime = captchaExpireTime;
    }

    public int getAccountExpireTime() {
        return accountExpireTime;
    }

    public void setAccountExpireTime(int accountExpireTime) {
        this.accountExpireTime = accountExpireTime;
    }

    public int getOrderExpireTime() {
        return orderExpireTime;
    }

    public void setOrderExpireTime(int orderExpireTime) {
        this.orderExpireTime = orderExpireTime;
    }

    @PostConstruct
    private void init() {
        redisStringCache = new RedisStringCache();
        redisStringCache.setTemplate(template);
        redisStringCache.setCaptchaExpireTime(captchaExpireTime);
        redisStringCache.setAccountExpireTime(accountExpireTime);
        redisStringCache.setOrderExpireTime(orderExpireTime);
    }

    public static void cache(String key, String value, CacheType cacheType) {
        int expireTime = 0;
        switch (cacheType) {
            case CAPTCHA:
                expireTime = redisStringCache.getCaptchaExpireTime();
                break;
            case ACCOUNT:
                expireTime = redisStringCache.getAccountExpireTime();
                break;
            case ORDER:
                expireTime = redisStringCache.getOrderExpireTime();
                break;
            case TRADE:
            case POSI:
                break;
            default:
                expireTime = 10;
        }
        redisStringCache.getTemplate().opsForValue()
                .set(cacheType.getType() + key, value, expireTime, TimeUnit.SECONDS);
    }

    public static void remove(String key, CacheType cacheType) {
        redisStringCache.getTemplate().delete(cacheType.getType() + key);
    }

    public static String get(String key, CacheType cacheType) {
        return redisStringCache.getTemplate().opsForValue().get(cacheType.getType() + key);
    }



}
