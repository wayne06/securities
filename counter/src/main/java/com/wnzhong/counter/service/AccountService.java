package com.wnzhong.counter.service;

import com.wnzhong.counter.bean.Account;

public interface AccountService {

    Account login(long uid, String password, String captcha, String captchaId);

    boolean accountExistInCache(String token);

}
