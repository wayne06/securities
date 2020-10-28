package thirdpart.bus;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;
import thirdpart.codec.MsgCodec;

@Log4j2
@RequiredArgsConstructor
public class MqttBusSender implements IBusSender {

    @NonNull
    private String ip;

    @NonNull
    private int port;

    @NonNull
    private MsgCodec msgCodec;

    @NonNull
    private Vertx vertx;

    ///////////////////////////////////////////startup//////////////////////////////////////////////////

    @Override
    public void startup() {
        mqttConnect();
    }

    private void mqttConnect() {
        MqttClient mqttClient = MqttClient.create(vertx);
        mqttClient.connect(port, ip, res -> {
            if (res.succeeded()) {
                log.info("Connect to mqtt bus [ip: {}, port: {}] succeed.", ip, port);
                sender = mqttClient;
            } else {
                log.info("Connect to mqtt bus [ip: {}, port: {}] failed.", ip, port);
                mqttConnect();
            }
        });
    }

    ///////////////////////////////////////////publish//////////////////////////////////////////////////

    private volatile MqttClient sender;

    @Override
    public void publish(CommonMsg commonMsg) {
        sender.publish(
                Short.toString(commonMsg.getMsgDst()),
                msgCodec.encodeToBuffer(commonMsg),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false);
    }
}
