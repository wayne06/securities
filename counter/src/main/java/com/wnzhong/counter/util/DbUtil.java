package com.wnzhong.counter.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.wnzhong.counter.bean.res.Account;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DbUtil {

    private static DbUtil dbUtil;

    private DbUtil() {}

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @PostConstruct
    private void init() {
        dbUtil = new DbUtil();
        dbUtil.setSqlSessionTemplate(this.sqlSessionTemplate);
    }

    @VisibleForTesting
    public static long getId() {
        Long res = dbUtil.getSqlSessionTemplate().selectOne("testMapper.queryBalance");
        if(res == null) {
            return -1;
        } else {
            return res;
        }
    }

    public static Account queryAccount(long uid, String password) {
        return dbUtil.getSqlSessionTemplate().selectOne(
                "userMapper.queryAccount",
                ImmutableMap.of("Uid", uid, "Password", password)
        );
    }

    public static void updateLoginTime(long uid, String nowDate, String nowTime) {
        dbUtil.getSqlSessionTemplate().update(
                "userMapper.updateLoginTime",
                ImmutableMap.of("Uid", uid, "ModifyDate", nowDate, "ModifyTime", nowTime)
        );
    }
}
