package com.wnzhong.counter.cache;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.wnzhong.counter.bean.pojo.StockInfo;
import com.wnzhong.counter.util.DbUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class StockCache {

    /**
     * HashMultimap<String, StockInfo>  -->  Map<String,List<StockInfo>>
     * 6  -->  600086,600025...
     */
    private HashMultimap<String, StockInfo> invertIndex = HashMultimap.create();

    public Collection<StockInfo> getStocks(String key) {
        return invertIndex.get(key);
    }

    @PostConstruct
    private void createInvertIndex() {
        log.info("start loading stock data from db...");
        long start = System.currentTimeMillis();

        // 1. 从数据库读取股票数据
        List<Map<String, Object>> result = DbUtil.getStockList();
        //System.out.println(result);
        if (CollectionUtils.isEmpty(result)) {
            log.error("No stock data find in db.");
            return;
        }

        // 2. 建立倒排索引
        for (Map<String, Object> res : result) {
            int code = Integer.parseInt(res.get("code").toString());
            String name = res.get("name").toString();
            String abbrname = res.get("abbrname").toString();
            StockInfo stock = new StockInfo(code, name, abbrname);
            List<String> codeMetas = splitData(String.format("%06d", code));
            List<String> abbrnameMetas = splitData(abbrname);
            codeMetas.addAll(abbrnameMetas);

            for (String key : codeMetas) {
                // 限制索引数据列表长度
                if (!CollectionUtils.isEmpty(invertIndex.get(key)) && invertIndex.get(key).size() > 10) {
                    continue;
                }
                invertIndex.put(key, stock);
            }
        }
        log.info("load complete, take " + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * payh -> p,pa,pay,payh, a,ay,ayh, y,yh, h
     * @param code
     * @return
     */
    private List<String> splitData(String code) {
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < code.length(); i++) {
            for (int j = i + 1; j < code.length() + 1; j++) {
                result.add(code.substring(i, j));
            }
        }
        return result;
    }

}
