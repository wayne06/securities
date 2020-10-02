package thirdpart.fetchserv;

import thirdpart.order.OrderCmd;

import java.util.List;

/**
 * 上下游通讯的接口，用于规范 rpc 通讯中上下游的通讯格式
 * @author wayne
 */
public interface FetchService {

    /**
     * 获取数据
     * @return
     */
    List<OrderCmd> fetchData();

}
