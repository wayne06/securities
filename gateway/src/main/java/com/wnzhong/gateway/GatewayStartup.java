package com.wnzhong.gateway;

import com.wnzhong.gateway.bean.GatewayConfig;
import lombok.extern.log4j.Log4j2;
import org.dom4j.DocumentException;
import thirdpart.checksum.ByteCheckSum;
import thirdpart.codec.BodyCodecImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author wayne
 */
@Log4j2
public class GatewayStartup {

    public static void main(String[] args) throws DocumentException {
        String configFileName = "gateway.xml";
        GatewayConfig config = new GatewayConfig();

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(System.getProperty("user.dir") + "\\" + configFileName);
            log.info("[gateway.xml] exist in jar path");
        } catch (FileNotFoundException e) {
            inputStream = GatewayStartup.class.getResourceAsStream("/" + configFileName);
            log.info("[gateway.xml] exist in jar file.");
        }
        config.initConfig(inputStream);

        //config.initConfig(GatewayStartup.class.getResource("/").getPath() + configFileName);

        config.setCheckSum(new ByteCheckSum());
        config.setBodyCodec(new BodyCodecImpl());
        config.startup();
    }

}
