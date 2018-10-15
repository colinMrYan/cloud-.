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
}
