package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLMatcher {
	
	//获取字符串里的网址信息
	public static final String URL_PATTERN= "^(((https?|ec[cm](-[0-9a-z]+)+|gs-msg)://[a-zA-Z0-9\\_\\-]+(\\.[a-zA-Z0-9\\_\\-]+)*(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?)" +
			"|([a-zA-Z0-9\\-\\_]+\\.)+([a-zA-Z\\-\\_]+)(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?|\\d+(\\.\\d+){3}(\\:\\d{2,4})?)$";
	public static ArrayList<String> getUrls(String args) {  
//		Pattern pattern = Pattern  
//                .compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");
//		Pattern pattern = Pattern.compile("(https?://)?\\w+(\\.\\w+)+(/\\S+)?");
//		Pattern pattern = Pattern.compile("(https?://)?[a-zA-Z0-9]*[a-zA-z]+[a-zA-Z0-9]*(\\.\\w+)+(/\\S+)?");
		Pattern pattern = Pattern.compile(URL_PATTERN);
		ArrayList<String> urlList = new ArrayList<String>();
        Matcher matcher = pattern.matcher(args);
        while (matcher.find()) {  
        	urlList.add(matcher.group(0));
        } 
        return urlList;
	}
	
}
