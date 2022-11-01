package com.inspur.emmcloud.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串操作类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class StrUtil {

    /**
     * 判断数组是否为空
     *
     * @param strArray
     * @return
     */
    public static boolean arrayIsNotNull(String[] strArray) {
        return (strArray != null && strArray.length > 0);
    }

    public static boolean strIsNotNull(String str) {
        return (str != null && !"".equals(str) && str.trim().length() > 0);
    }

    // 判断字符串中是否存在中文
    public static boolean checkCN(String urlString) {
        // 用GBK编码
        urlString = new String(urlString.getBytes());
        String pattern = "[\u4e00-\u9fa5]+";
        Pattern p = Pattern.compile(pattern);
        Matcher result = p.matcher(urlString);
        // 是否含有中文字符
        return result.find();
    }

    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    /**
     * 判断字符串是否含有中文，含有的话进行编码
     *
     * @param urlString
     * @return
     */
    public static String changeUrl(String urlString) {
        urlString = urlString.trim();
        if (checkCN(urlString)) {
            try {
                urlString = URLEncoder.encode(urlString, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            urlString = urlString.replaceAll("\\+",
                    "%20").replaceAll("%3A", ":").replaceAll("%2F", "/");
        }
        return urlString;
    }


    /**
     * 对中文字符进行UTF-8编码
     *
     * @param source 要转义的字符串
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String tranformStyle(String source){
        char[] arr = source.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            char temp = arr[i];
            if (isChinese(temp)) {
                try {
                    sb.append(URLEncoder.encode("" + temp, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                continue;
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

}
