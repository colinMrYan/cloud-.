package com.inspur.emmcloud.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionsMatcher {

	// 获取字符串里的网址信息
	public static String handleMentioin(String mentions) {
	    Pattern pattern = Pattern.compile("\\[@[^\\]]+\\]\\([^\\)]+\\)");
	    if(StringUtils.isBlank(mentions)){
	    	return "";
	    }
		Matcher matcher = pattern.matcher(mentions);
		while (matcher.find()) {
			String mention = matcher.group();
			String pro = "";
			String name = "";
			
			Pattern pattern2 = Pattern.compile("\\(.*\\)");
			Matcher matcher2 = pattern2.matcher(mention);
			while (matcher2.find()) {
				pro = matcher2.group();
				pro = pro.substring(1, pro.length()-1);
			}
			Pattern pattern3 = Pattern.compile("\\[.*\\]");
			Matcher matcher3 = pattern3.matcher(mention);
			while (matcher3.find()) {
				name = matcher3.group();
				name = name.substring(1, name.length()-1);
			}
			mentions = mentions.replace(mention, "<a href="+ "\""+pro+"\""+" style=\"text-decoration:none;color:red\"" +">"+name+"</a>");
		}
		
		Pattern urlpattern = Pattern  
                .compile("\\[[^@][^\\]]+\\]\\([^\\)]+\\)");
        Matcher urlmatcher = urlpattern  
                .matcher(mentions);
        while (urlmatcher.find()) {
			String url = urlmatcher.group();
			String pro = "";
			String name = "";
			Pattern pattern2 = Pattern.compile("\\(.*\\)");
			Matcher matcher2 = pattern2.matcher(url);
			while (matcher2.find()) {
				pro = matcher2.group();
				pro = pro.substring(1, pro.length()-1);
			}
			
			Pattern pattern3 = Pattern.compile("\\[.*\\]");
			Matcher matcher3 = pattern3.matcher(url);
			while (matcher3.find()) {
				name = matcher3.group();
				name = name.substring(1, name.length()-1);
			}
			mentions = mentions.replace(url, "<a href="+ "\""+pro+"\"" +">"+name+"</a>");
		}

		return mentions;
	}
}
