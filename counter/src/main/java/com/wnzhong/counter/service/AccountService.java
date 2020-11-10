package com.wnzhong.counter.service;

import com.wnzhong.counter.bean.pojo.Account;

public interface AccountService {

    Account login(long uid, String password, String captcha, String captchaId);

    boolean accountExistInCache(String token);

    boolean logout(String token);

    boolean changePassword(long uid, String oldPass, String newPass);
}
