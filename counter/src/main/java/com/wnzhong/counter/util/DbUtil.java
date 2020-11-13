package com.wnzhong.counter.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wnzhong.counter.bean.pojo.Account;
import com.wnzhong.counter.bean.pojo.OrderInfo;
import com.wnzhong.counter.bean.pojo.PosiInfo;
import com.wnzhong.counter.bean.pojo.TradeInfo;
import com.wnzhong.counter.cache.CacheType;
import com.wnzhong.counter.cache.RedisStringCache;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import thirdpart.hq.MatchData;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderStatus;

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

    /**
     * @param uid
     * @param variation 卖出为正，买入为负
     */
    public static void updateBalance(long uid, long variation) {
        dbUtil.getSqlSessionTemplate().update(
                "orderMapper.updateBalance",
                ImmutableMap.of("Uid", uid, "Variation", variation)
        );
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

    /**
     * 1. 持仓中没有该股票，则创建持仓
     * 2. 持仓中有该股票，则更新持仓
     * @param uid
     * @param stockCode
     * @param tradingVolume 买入为正，卖出为负
     * @param pricePerShare
     */
    public static void updateOrCreatePosi(long uid, int stockCode, long tradingVolume, long pricePerShare) {
        PosiInfo posiInfo = getPosi(uid, stockCode);
        if (posiInfo == null) {
            insertPosi(uid, stockCode, tradingVolume, pricePerShare);
        } else {
            posiInfo.setCount(posiInfo.getCount() + tradingVolume);
            posiInfo.setCost(posiInfo.getCost() + pricePerShare * tradingVolume);
            updatePosi(posiInfo);
        }
    }

    private static void updatePosi(PosiInfo posiInfo) {
        dbUtil.getSqlSessionTemplate().update(
                "orderMapper.updatePosi",
                ImmutableMap.of("Uid", posiInfo.getUid(),
                        "StockCode", posiInfo.getCode(),
                        "TradingVolume", posiInfo.getCount(),
                        "Cost", posiInfo.getCost())
        );
    }

    private static void insertPosi(long uid, int stockCode, long tradingVolume, long pricePerShare) {
        dbUtil.getSqlSessionTemplate().insert(
                "orderMapper.insertPosi",
                ImmutableMap.of("Uid", uid,
                        "StockCode", stockCode,
                        "TradingVolume", tradingVolume,
                        "Cost", pricePerShare * tradingVolume)
        );
    }

    private static PosiInfo getPosi(long uid, int stockCode) {
        return dbUtil.getSqlSessionTemplate().selectOne(
                "orderMapper.queryPosi",
                ImmutableMap.of("Uid", uid, "StockCode", stockCode)
        );
    }

    //********************************** 委托 **********************************

    public static List<OrderInfo> getOrderList(long uid) {
        String uidStr = Long.toString(uid);
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

    public static void updateOrder(long uid, int oid, OrderStatus status) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("Id", oid);
        param.put("Status", status.getCode());
        dbUtil.getSqlSessionTemplate().update("orderMapper.updateOrder", param);

        RedisStringCache.remove(Long.toString(uid), CacheType.ORDER);
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

    public static void saveTrade(int counterOId, MatchData matchData, OrderCmd orderCmd) {
        if (orderCmd == null) {
            return;
        }
        Map<String, Object> param = Maps.newHashMap();
        param.put("Id", matchData.tid);
        param.put("UId", orderCmd.uid);
        param.put("Code", orderCmd.code);
        param.put("Direction", orderCmd.direction.getDirection());
        param.put("Price", matchData.price);
        param.put("TCount", matchData.volume);
        param.put("OId", counterOId);
        param.put("Date", TimeUtil.yyyyMMdd(matchData.timestamp));
        param.put("Time", TimeUtil.hhMMss(matchData.timestamp));
        dbUtil.getSqlSessionTemplate().insert("orderMapper.saveTrade", param);

        //更新缓存
        RedisStringCache.remove(Long.toString(orderCmd.uid), CacheType.TRADE);
    }

    //********************************** 股票信息查询 **********************************

    public static List<Map<String, Object>> getStockList() {
        return dbUtil.getSqlSessionTemplate().selectList("stockMapper.queryStock");
    }

    //********************************** 订单处理（发送保存委托） **********************************

    public static int saveOrder(OrderCmd orderCmd) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("UId", orderCmd.uid);
        param.put("Code", orderCmd.code);
        param.put("Direction", orderCmd.direction.getDirection());
        param.put("Type", orderCmd.orderType.getType());
        param.put("Price", orderCmd.price);

        // 委托量
        param.put("OCount", orderCmd.volume);

        // 成交量：委托刚到柜台，所以默认为0
        param.put("TCount", 0);

        // 未报状态
        param.put("Status", OrderStatus.NOT_SET.getCode());

        param.put("Date", TimeUtil.yyyyMMdd(orderCmd.timestamp));
        param.put("Time", TimeUtil.hhMMss(orderCmd.timestamp));

        int count = dbUtil.getSqlSessionTemplate().insert("orderMapper.saveOrder", param);
        if (count > 0) {
            return Integer.parseInt(param.get("ID").toString());
        } else {
            return -1;
        }

    }

}
