package com.wnzhong.counter.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.Date;

public class TimeUtil {

    public static final String YYYY_MM_DD = "yyyyMMdd";
    public static final String HH_MM_SS = "HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyyMMdd HH:mm:ss";

    public static String yyyyMMdd(Date date) {
        return DateFormatUtils.format(date, YYYY_MM_DD);
    }

    public static String hhMMss(Date date) {
        return DateFormatUtils.format(date, HH_MM_SS);
    }

    public static String hhMMss(long timestamp) {
        return DateFormatUtils.format(timestamp, HH_MM_SS);
    }

    public static String yyyyMMddHHmmss(Date date) {
        return DateFormatUtils.format(date, YYYY_MM_DD_HH_MM_SS);
    }
}
