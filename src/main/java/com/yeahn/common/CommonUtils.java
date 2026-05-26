package com.yeahn.common;

import com.navercorp.lucy.security.xss.servletfilter.defender.XssPreventerDefender;
import com.nhncorp.lucy.security.xss.XssFilter;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    //사용자 IP가져오기
    public static String getIP(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-RealIP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if(ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1"))
        {
            try {
                InetAddress address = InetAddress.getLocalHost();
                ip = address.getHostAddress();
            } catch (Exception e) {
            }
        }

        return ip;
    }
}
