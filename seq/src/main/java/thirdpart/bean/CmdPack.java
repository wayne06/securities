package thirdpart.bean;

import lombok.*;
import thirdpart.order.OrderCmd;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CmdPack implements Serializable {

    /**
     * 包号：下游撮合核心会根据包号判断是否有乱序或丢包情况
     */
    private long packNo;

    /**
     * 委托数据集合
     */
    private List<OrderCmd> orderCmds;

}
