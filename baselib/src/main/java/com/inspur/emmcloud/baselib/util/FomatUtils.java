package com.inspur.emmcloud.baselib.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FomatUtils {
    public static Boolean isPhoneNum(String phoneNum) {
        Pattern p = Pattern
                .compile("^([1])\\d{10}$");
        Matcher m = p.matcher(phoneNum);
        return m.matches();
    }

    /**
     * 验证文件名是否合法
     *
     * @param fileName
     * @return
     */
    public static boolean isValidFileName(String fileName) {
        Matcher matcher = Pattern.compile(
                "[\\\\/:*?\"<>|&]").matcher(
                fileName);

        while (matcher.find()) {
            return false;
        }
        return true;
    }

    public static boolean isValiadEmail(String email) {
        Matcher matcher = Pattern.compile(
                "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$").matcher(
                email);

        while (matcher.find()) {
            return true;
        }
        return false;
    }


    /**
     * 只含有字母和数字
     *
     * @param string
     * @return
     */
    public static boolean isLetterOrDigits(String string) {
        boolean flag = false;
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLowerCase(string.charAt(i))
                    || Character.isUpperCase(string.charAt(i))
                    || Character.isDigit(string.charAt(i))) {
                flag = true;
            } else {
                flag = false;
                return flag;
            }
        }
        return flag;
    }

    /**
     * 判断密码是否足够复杂（数字，大写字母，小写字母，特殊符号   4选3  大于8 位）
     *
     * @param password
     * @return
     */
    public static boolean isPasswrodStrong(String password) {
        int strongDigit = 0;
        int strongUpperCase = 0;
        int strongLowerCase = 0;
        int strongSpecial = 0;
        for (int i = 0; i < password.length(); i++) {
            int A = password.charAt(i);
            if (A >= 48 && A <= 57) {// 数字
                strongDigit = 1;
            } else if (A >= 65 && A <= 90) {// 大写
                strongUpperCase = 1;
            } else if (A >= 97 && A <= 122) {// 小写
                strongLowerCase = 1;
            } else {
                strongSpecial = 1;
            }
        }
        int strong = strongDigit + strongLowerCase + strongUpperCase + strongSpecial;
        return (strong >= 2);
    }
}
