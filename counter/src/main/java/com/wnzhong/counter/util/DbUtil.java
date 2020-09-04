package com.wnzhong.counter.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.wnzhong.counter.bean.Account;
import com.wnzhong.counter.bean.OrderInfo;
import com.wnzhong.counter.bean.PosiInfo;
import com.wnzhong.counter.bean.TradeInfo;
import com.wnzhong.counter.cache.CacheType;
import com.wnzhong.counter.cache.RedisStringCache;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

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

    //********************************** 认证 **********************************

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

    public static int updatePassword(long uid, String oldPass, String newPass) {
        return dbUtil.getSqlSessionTemplate().update(
                "userMapper.updatePassword",
                ImmutableMap.of("Uid", uid, "OldPass", oldPass, "NewPass", newPass)
        );
    }

    //********************************** 资金 **********************************

    public static long getBalance(long uid) {
        Long balance = dbUtil.getSqlSessionTemplate().selectOne(
                "orderMapper.queryBalance",
                ImmutableMap.of("Uid", uid)
        );
        return balance == null ? -1 : balance;
    }

    //********************************** 持仓 **********************************

    public static List<PosiInfo> getPosiList(long uid) {
        String uidStr = String.valueOf(uid);
        String posiInCache = RedisStringCache.get(uidStr, CacheType.POSI);
        if (StringUtils.isEmpty(posiInCache)) {
            List<PosiInfo> posiInfos = dbUtil.getSqlSessionTemplate().selectList(
                    "orderMapper.queryPosi",
                    ImmutableMap.of("Uid", uid)
            );
            List<PosiInfo> res = CollectionUtils.isEmpty(posiInfos) ? Lists.newArrayList() : posiInfos;
            RedisStringCache.cache(uidStr, JsonUtil.toJson(posiInfos), CacheType.POSI);
            return res;
        } else {
            return JsonUtil.fromJsonArr(posiInCache, PosiInfo.class);
        }
    }

    //********************************** 委托 **********************************

    public static List<OrderInfo> getOrderList(long uid) {
        String uidStr = String.valueOf(uid);
        String orderInCache = RedisStringCache.get(uidStr, CacheType.ORDER);
        if (StringUtils.isEmpty(orderInCache)) {
            List<OrderInfo> orderInfos = dbUtil.getSqlSessionTemplate().selectList(
                    "orderMapper.queryOrder",
                    ImmutableMap.of("Uid", uid)
            );
            List<OrderInfo> res = CollectionUtils.isEmpty(orderInfos) ? Lists.newArrayList() : orderInfos;
            RedisStringCache.cache(uidStr, JsonUtil.toJson(orderInfos), CacheType.ORDER);
            return res;
        } else {
            return JsonUtil.fromJsonArr(orderInCache, OrderInfo.class);
        }
    }

    //********************************** 成交 **********************************

    public static List<TradeInfo> getTradeList(long uid) {
        String uidStr = String.valueOf(uid);
        String tradeInCache = RedisStringCache.get(uidStr, CacheType.TRADE);
        if (StringUtils.isEmpty(tradeInCache)) {
            List<TradeInfo> tradeInfos = dbUtil.getSqlSessionTemplate().selectList(
                    "orderMapper.queryTrade",
                    ImmutableMap.of("Uid", uid)
            );
            List<TradeInfo> res = CollectionUtils.isEmpty(tradeInfos) ? Lists.newArrayList() : tradeInfos;
            RedisStringCache.cache(uidStr, JsonUtil.toJson(res), CacheType.TRADE);
            return res;
        } else {
            return JsonUtil.fromJsonArr(tradeInCache, TradeInfo.class);
        }

    }

    //********************************** 股票信息查询 **********************************

    public static List<Map<String, Object>> getStockList() {
        return dbUtil.getSqlSessionTemplate().selectList("stockMapper.queryStock");
    }

}
