package thirdpart.checksum;

public class ByteCheckSum implements CheckSum {

    @Override
    public byte getCheckSum(byte[] data) {
        byte sum = 0;
        for (byte b : data) {
            sum ^= b;
        }
        return sum;
    }

}
