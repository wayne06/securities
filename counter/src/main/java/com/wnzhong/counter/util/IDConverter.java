package com.wnzhong.counter.util;

/**
 * 拼接两个整型为一个长整型，和拆分一个长整型为两个整型
 */
public class IDConverter {

    /**
     * 拼接长整型：high左移32位，然后与low做或运算
     * @param high
     * @param low
     * @return
     */
    public static long combineInt2Long(int high, int low) {
        return ((long) high << 32 & 0xFFFFFFFF00000000L) | ((long) low & 0xFFFFFFFFL);
    }

    /**
     * 拆分长整型
     * @param val
     * @return
     */
    public static int[] seperateLong2Int(long val) {
        int[] res = new int[2];
        res[1] = (int) (val & 0xFFFFFFFFL);
        res[0] = (int) ((val & 0xFFFFFFFF00000000L) >> 32);
        return res;
    }

    public static void main(String[] args) {
        int a = 1001;
        int b = 200;
        System.out.println("Binary of a: " + Integer.toBinaryString(a));
        System.out.println("Binary of b: " + Integer.toBinaryString(b));
        long l = combineInt2Long(a, b);
        System.out.println("Long of l: " + l);
        System.out.println(Long.toBinaryString(l));
        int[] ints = seperateLong2Int(l);
        System.out.println(ints[0]);
        System.out.println(ints[1]);
    }

}
