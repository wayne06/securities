package thirdpart;

import com.alipay.remoting.exception.CodecException;

import java.io.Serializable;
import java.util.Arrays;

class BodyCodecImplTest {

    static class A implements Serializable {
        String a;
    }

    public static void main(String[] args) throws CodecException {
        A a = new A();
        a.a = "test";

        byte[] serialize = new BodyCodecImpl().serialize(a);
        System.out.println(Arrays.toString(serialize));

        A deserialize = new BodyCodecImpl().deserialize(serialize, A.class);
        System.out.println(deserialize);

        System.out.println(deserialize.a);
    }

}
