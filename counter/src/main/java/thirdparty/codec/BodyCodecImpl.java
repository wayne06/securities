package thirdparty.codec;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;

public class BodyCodecImpl implements BodyCodec {
    @Override
    public <T> byte[] serialize(T object) throws CodecException {
        // 方式一：jdk序列化（性能不高、序列化后的字节数组太大不适合网络传输）
        // 方式二：json（易被抓包安全性差、可读性强）
        // 方式三：自定义算法（适合对性能、安全有要求的场景）（Hessian2）
        return SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws CodecException {
        return SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(bytes, clazz.getName());
    }
}
