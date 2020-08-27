package com.wnzhong.counter.controller;

import com.wnzhong.counter.bean.res.CaptchaRes;
import com.wnzhong.counter.bean.res.CounterRes;
import com.wnzhong.counter.cache.CacheType;
import com.wnzhong.counter.cache.RedisStringCache;
import com.wnzhong.counter.util.Captcha;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thirdparty.uuid.MyUuid;

import java.io.IOException;

@RestController
@RequestMapping("/login")
@Log4j2
public class LoginController {

    @RequestMapping("/captcha")
    public CounterRes captcha() throws IOException {
        // 1.生成验证码：120*40px，4个字符，噪点+线条
        Captcha captcha = new Captcha(120, 40, 4, 10);

        // 2.将验证码<ID，验证码数值>放入缓存
        String id = String.valueOf(MyUuid.getInstance().getUuid());
        RedisStringCache.cache(id, captcha.getCode(), CacheType.CAPTCHA);

        // 3.使用base64编码图片，并返回给前台
        CaptchaRes res = new CaptchaRes(id, captcha.getBase64ByteStr());
        return new CounterRes(res);
    }

    @RequestMapping("/userlogin")
    public CounterRes login() {
        return null;
    }
}
