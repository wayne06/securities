package thirdpart.codec;

import com.alipay.remoting.exception.CodecException;

public interface BodyCodec {

    <T> byte[] serialize(T object) throws CodecException;


    <T> T deserialize(byte[] bytes, Class<T> clazz) throws CodecException;

}
