package com.yeahn.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
    // Unix timestamp 를 Date String으로 변환하는 함수
    public static String getTimestampToDate(Long timestamp){
        Date date = new java.util.Date(timestamp*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }
}
