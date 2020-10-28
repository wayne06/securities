package com.wnzhong.engine.handler.risk;

import com.wnzhong.engine.bean.command.CmdResultCode;
import com.wnzhong.engine.bean.command.RbCmd;
import com.wnzhong.engine.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import thirdpart.order.CmdType;

/**
 * @author wayne
 */
@Log4j2
@RequiredArgsConstructor
public class ExistRiskHandler extends BaseHandler {

    @NonNull
    private MutableLongSet uidSet;

    @NonNull
    private MutableIntSet codeSet;

    /**
     * @param event 发布行情event，新委托event，撤单event，权限控制，系统关机...
     * @param sequence
     * @param endOfBatch
     * @throws Exception
     */
    @Override
    public void onEvent(RbCmd event, long sequence, boolean endOfBatch) throws Exception {
        //系统内部的指令，前置风控模块直接忽略
        if (event.command == CmdType.HQ_PUB) {
            return;
        }

        if (event.command == CmdType.NEW_ORDER || event.command == CmdType.CANCEL_ORDER) {

            //1.用户是否存在
            if (!uidSet.contains(event.uid)) {
                log.error("Illegal uid [{}] exist.", event.uid);
                event.resultCode = CmdResultCode.RISK_INVALID_USER;
                return;
            }

            //2.股票代码是否合法
            if (!codeSet.contains(event.code)) {
                log.error("Illegal code [{}] exist.", event.code);
                event.resultCode = CmdResultCode.RISK_INVALID_CODE;
                return;
            }
        }
    }
}
