package thirdparty.checksum;

public class ByteCheckSum implements CheckSum {

    @Override
    public byte getSum(byte[] data) {
        byte sum = 0;
        for (byte b : data) {
            sum ^= b;
        }
        return sum;
    }

}
