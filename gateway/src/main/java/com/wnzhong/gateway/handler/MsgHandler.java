package com.wnzhong.gateway.handler;

import io.vertx.core.net.NetSocket;
import thirdpart.bean.CommonMsg;

public interface MsgHandler {

    default void onConnect(NetSocket netSocket) {

    }

    default void onDisconnect(NetSocket netSocket) {

    }

    default void onException(NetSocket netSocket, Throwable e) {

    }

    void onCounterData(CommonMsg commonMsg);

}
