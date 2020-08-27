package com.wnzhong.counter.controller;

import com.wnzhong.counter.bean.res.CounterRes;
import com.wnzhong.counter.util.Captcha;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Log4j2
public class LoginController {

    @RequestMapping("/captcha")
    public CounterRes captcha() {
        // 1.生成验证码：120*40px，4个字符，噪点+线条
        Captcha captcha = new Captcha(120, 40, 4, 10);

        // 2.将验证码<ID，验证码数值>放入缓存

        // 3.使用base64编码图片，并返回给前台

        return null;
    }

    @RequestMapping("/userlogin")
    public CounterRes login() {
        return null;
    }
}
