package thirdpart.checksum;

class ByteCheckSumTest {

    public static void main(String[] args) {
        String a = "test";
        String b = "test1";
        String c = "test";

        byte ca = new ByteCheckSum().getSum(a.getBytes());
        byte cb = new ByteCheckSum().getSum(b.getBytes());
        byte cc = new ByteCheckSum().getSum(c.getBytes());

        System.out.println(ca);
        System.out.println(cb);
        System.out.println(cc);
    }
}
