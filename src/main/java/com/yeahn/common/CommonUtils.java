package com.yeahn.common;

import com.navercorp.lucy.security.xss.servletfilter.defender.XssPreventerDefender;
import com.nhncorp.lucy.security.xss.XssFilter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CommonUtils {
    // Unix timestamp 를 Date String으로 변환하는 함수
    public static String getTimestampToDate(Long timestamp){
        Date date = new java.util.Date(timestamp*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    // XSS처리
    public static Map<String, Object> paramCleanXSS(Map<String, Object> params) {
        Map<String, Object> returnParams = new HashMap<String, Object>();
        Iterator<String> keys = params.keySet().iterator();
        XssPreventerDefender defender = new XssPreventerDefender();
        XssFilter filter = XssFilter.getInstance("lucy-xss-superset.xml",true);
        while(keys.hasNext()){
            String key = keys.next();
            if(params.containsKey(key)){
                if(params.get(key)!=null&&!"".equals((""+params.get(key)).trim())){
                    //예외 파라메터 설정
                    Object reParam = (Object)String.valueOf(params.get(key));
                    if(!((key).equals("password")||(key).equals("PWD")||(key).equals("PASSWD")||(key).equals("CAPTCHA_ANSWER")||(key).equals("SUBMIT_KEY"))){
                        reParam = defender.doFilter(String.valueOf(reParam).trim());
                        reParam = filter.doFilter(String.valueOf(reParam));
                    }

                    returnParams.put(key, reParam);
                }
            }
        }
        return returnParams;
    }
}
