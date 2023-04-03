package com.ark.monitor.bug.repoter.util;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;


public class IpUtil {

    private static final Integer MAX_IP_LENGTH = 23;
    /**
     * IP白名单
     */
    private static final Set<String> WHITE_ADDRESS = Sets.newHashSet();

    static {
        WHITE_ADDRESS.add("192.168.");
    }

    /**
     * 判断是否为白名单IP
     *
     * @param ipv4
     * @return
     */
    public static final boolean whiteOf(String ipv4) {
        for (String ipField : WHITE_ADDRESS) {
            if (ipv4.startsWith(ipField)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过指定请求获得对应的远程ip地址
     */
    public static String getRemoteIP(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-real-ip");
            if (StringUtils.isEmpty(ip)) {
                ip = request.getRemoteAddr();
            }

            //过滤反向代理的ip
            String[] stemps = ip.split(",");
            if (stemps != null && stemps.length >= 1) {
                //得到第一个IP，即客户端真实IP
                ip = stemps[0];
            }

            ip = ip.trim();
            if (ip.length() > MAX_IP_LENGTH) {
                ip = ip.substring(0, MAX_IP_LENGTH);
            }
            return ip;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getIpAdrress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if (index != -1) {
                return XFor.substring(0, index);
            } else {
                return XFor;
            }
        }
        XFor = Xip;
        if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            return XFor;
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }

}
