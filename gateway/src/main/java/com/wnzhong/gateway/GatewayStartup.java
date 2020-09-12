package com.wnzhong.gateway;

import com.wnzhong.gateway.bean.GatewayConfig;
import lombok.extern.log4j.Log4j2;
import org.dom4j.DocumentException;
import thirdpart.checksum.ByteCheckSum;
import thirdpart.codec.BodyCodecImpl;

@Log4j2
public class GatewayStartup {

    public static void main(String[] args) throws DocumentException {
        String configFileName = "gateway.xml";
        GatewayConfig config = new GatewayConfig();
        config.initConfig(GatewayStartup.class.getResource("/").getPath() + configFileName);
        config.setCheckSum(new ByteCheckSum());
        config.setBodyCodec(new BodyCodecImpl());
        config.startup();
    }

}
