package com.dafuweng.gateway.filter;

/**
 * XSS清理工具类
 */
public class XssUtils {
    
    /**
     * 清理XSS脚本
     */
    public static String cleanXss(String value) {
        if (value == null) {
            return null;
        }
        
        // 移除<script>标签
        value = value.replaceAll("(?i)<script[^>]*>(.*?)</script>", "");
        
        // 移除javascript:协议
        value = value.replaceAll("(?i)javascript:", "");
        
        // 移除vbscript:协议
        value = value.replaceAll("(?i)vbscript:", "");
        
        // 移除on*事件处理器
        value = value.replaceAll("(?i)on\\w+\\s*=\\s*\"[^\"]*\"", "");
        value = value.replaceAll("(?i)on\\w+\\s*=\\s*'[^']*'", "");
        
        // 移除危险标签
        value = value.replaceAll("(?i)<(iframe|object|embed|applet|form|input|button|textarea|select)[^>]*>.*?</\\1>", "");
        value = value.replaceAll("(?i)</?(iframe|object|embed|applet|form|input|button|textarea|select)[^>]*>", "");
        
        return value;
    }
    
    /**
     * 检查是否包含XSS攻击
     */
    public static boolean containsXss(String value) {
        if (value == null) {
            return false;
        }
        
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("<script") ||
               lowerValue.contains("javascript:") ||
               lowerValue.contains("vbscript:") ||
               lowerValue.contains("onload") ||
               lowerValue.contains("onerror") ||
               lowerValue.contains("onclick") ||
               lowerValue.contains("<iframe") ||
               lowerValue.contains("<object") ||
               lowerValue.contains("<embed");
    }
}
