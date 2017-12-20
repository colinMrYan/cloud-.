package com.inspur.emmcloud.util.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FomatUtils {
	public static Boolean isPhoneNum(String phoneNum) {
		Pattern p = Pattern
				.compile("^([1][345678])\\d{9}$");
		Matcher m = p.matcher(phoneNum);
		return m.matches();
	}

	/**
	 * 验证文件名是否合法
	 * @param fileName
	 * @return
	 */
	public static boolean isValidFileName(String fileName){
		return  !fileName.matches("[\\\\/:*?\"<>|]");
	}
}
