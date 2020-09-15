package com.wnzhong.gateway.handler;

import io.vertx.core.net.NetSocket;
import thirdpart.bean.CommonMsg;

public interface MsgHandler {

    /**
     * 接收连接的消息
     * @param netSocket
     */
    default void onConnect(NetSocket netSocket) {

    }

    /**
     * 断开的消息
     * @param netSocket
     */
    default void onDisconnect(NetSocket netSocket) {

    }

    /**
     * 异常的消息
     * @param netSocket
     * @param e
     */
    default void onException(NetSocket netSocket, Throwable e) {

    }

    /**
     * 柜台发过来的消息
     * @param commonMsg
     */
    void onCounterData(CommonMsg commonMsg);

}
