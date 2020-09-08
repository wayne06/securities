package thirdpart.checksum;

class ByteCheckSumTest {

    public static void main(String[] args) {
        String a = "test";
        String b = "test1";
        String c = "test";

        byte ca = new ByteCheckSum().getCheckSum(a.getBytes());
        byte cb = new ByteCheckSum().getCheckSum(b.getBytes());
        byte cc = new ByteCheckSum().getCheckSum(c.getBytes());

        System.out.println(ca);
        System.out.println(cb);
        System.out.println(cc);
    }
}
