package com.wnzhong.seq.startup;

import com.wnzhong.seq.config.SeqConfig;
import thirdpart.codec.BodyCodecImpl;

import java.io.IOException;

public class SeqStartup3 {

    public static void main(String[] args) throws IOException {
        String configName = "seq3.properties";
        new SeqConfig(new BodyCodecImpl(), configName).startup();
    }

}
