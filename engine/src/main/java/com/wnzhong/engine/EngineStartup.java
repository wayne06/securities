package com.wnzhong.engine;

import com.wnzhong.engine.config.EngineConfig;
import thirdpart.checksum.ByteCheckSum;
import thirdpart.codec.BodyCodecImpl;
import thirdpart.codec.MsgCodecImpl;

import java.io.IOException;

public class EngineStartup {

    public static void main(String[] args) throws IOException {
        new EngineConfig("engine.properties",
                new BodyCodecImpl(),
                new ByteCheckSum(),
                new MsgCodecImpl()
        ).startup();
    }

}
