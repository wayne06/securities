package com.wnzhong.counter.controller;

import com.wnzhong.counter.bean.Account;
import com.wnzhong.counter.bean.res.CaptchaRes;
import com.wnzhong.counter.bean.res.CounterRes;
import com.wnzhong.counter.cache.CacheType;
import com.wnzhong.counter.cache.RedisStringCache;
import com.wnzhong.counter.service.AccountService;
import com.wnzhong.counter.util.Captcha;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import thirdparty.uuid.MyUuid;

import java.io.IOException;

@RestController
@RequestMapping("/login")
@Log4j2
public class LoginController {

    @Autowired
    private AccountService accountService;

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
    public CounterRes login(@RequestParam long uid, @RequestParam String password,
                            @RequestParam String captcha, @RequestParam String captchaId) {
        Account account = accountService.login(uid, password, captcha, captchaId);
        if (account == null) {
            return new CounterRes(CounterRes.FAIL, "login failed.", null);
        } else {
            return new CounterRes(account);
        }
    }

    @RequestMapping("/loginfail")
    public CounterRes loginFail() {
        return new CounterRes(CounterRes.RELOGIN, "please retry.", null);
    }

    @RequestMapping("/logout")
    public CounterRes logout(@RequestParam String token) {
        accountService.logout(token);
        return new CounterRes(CounterRes.SUCCESS, "logout success.", null);
    }

    @RequestMapping("/changepass")
    public CounterRes changePass(@RequestParam long uid, @RequestParam String oldPass, @RequestParam String newPass) {
        boolean res = accountService.changePassword(uid, oldPass, newPass);
        if (res) {
            return new CounterRes(CounterRes.SUCCESS, "password updated.", null);
        } else {
            return new CounterRes(CounterRes.FAIL, "password update failed.", null);
        }
    }
}
