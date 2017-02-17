package com.inspur.emmcloud.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FomatUtils {
	public static Boolean isPhoneNum(String phoneNum) {
		Pattern p = Pattern
				.compile("^([1][345678])\\d{9}$");
		Matcher m = p.matcher(phoneNum);
		return m.matches();
	}
}
