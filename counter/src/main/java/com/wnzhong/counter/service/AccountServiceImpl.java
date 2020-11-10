package com.wnzhong.counter.service;

import com.wnzhong.counter.bean.pojo.Account;
import com.wnzhong.counter.cache.CacheType;
import com.wnzhong.counter.cache.RedisStringCache;
import com.wnzhong.counter.util.DbUtil;
import com.wnzhong.counter.util.JsonUtil;
import com.wnzhong.counter.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import thirdpart.uuid.MyUuid;

import java.util.Date;

@Service
public class AccountServiceImpl implements AccountService {

    @Override
    public Account login(long uid, String password, String captcha, String captchaId) {
        // 入参校验
        if (StringUtils.isAnyBlank(password, captcha, captchaId)) {
            return null;
        }

        // 缓存验证码校验
        String captchaInCache = RedisStringCache.get(captchaId, CacheType.CAPTCHA);
        if (StringUtils.isBlank(captchaInCache) || !StringUtils.equalsIgnoreCase(captcha, captchaInCache)) {
            return null;
        }
        RedisStringCache.remove(captchaId, CacheType.CAPTCHA);

        // 数据库中用户名、密码校验
        Account account = DbUtil.queryAccount(uid, password);
        if (account == null) {
            return null;
        } else {
            // 设置token，并存入缓存
            account.setToken(String.valueOf(MyUuid.getInstance().getUuid()));
            RedisStringCache.cache(account.getToken(), JsonUtil.toJson(account), CacheType.ACCOUNT);
            // 更新登录时间
            Date date = new Date();
            DbUtil.updateLoginTime(uid, TimeUtil.yyyyMMdd(date), TimeUtil.hhMMss(date));
            return account;
        }
    }

    @Override
    public boolean accountExistInCache(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        String account = RedisStringCache.get(token, CacheType.ACCOUNT);
        if (account != null) {
            RedisStringCache.cache(token, account, CacheType.ACCOUNT);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean logout(String token) {
        RedisStringCache.remove(token, CacheType.ACCOUNT);
        return true;
    }

    @Override
    public boolean changePassword(long uid, String oldPass, String newPass) {
        int res = DbUtil.updatePassword(uid, oldPass, newPass);
        return res == 0 ? false : true;
    }
}
