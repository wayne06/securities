package com.wnzhong.engine.handler;

import com.lmax.disruptor.EventHandler;
import com.wnzhong.engine.bean.command.RbCmd;

/**
 * @author wayne
 */
public abstract class BaseHandler implements EventHandler<RbCmd> {

}
