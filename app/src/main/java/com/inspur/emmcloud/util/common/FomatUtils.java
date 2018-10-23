package com.inspur.emmcloud.util.common;

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
	 * @param fileName
	 * @return
	 */
	public static boolean isValidFileName(String fileName){
		Matcher matcher = Pattern.compile(
				"[\\\\/:*?\"<>|]" ).matcher(
				fileName );

		while ( matcher.find() )
		{
			return false;
		}
		return true;
	}

	public static  boolean isValiadEmail(String email){
		Matcher matcher = Pattern.compile(
				"^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$" ).matcher(
				email );

		while ( matcher.find() )
		{
			return true;
		}
		return false;
	}


	/**
	 * 只含有字母和数字
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
}
