package com.inspur.emmcloud.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandleMsgTextUtils {

	
	public static String handleMentionAndURL(String content,
			List<String> userNameList, List<String> uidList
			) {
		String temp = "";
		temp = content;
		
		if (userNameList.size() > 0) {
			for (int i = 0; i < userNameList.size(); i++) {
				temp = temp.toString().replaceFirst(
						userNameList.get(i),
						"["+ userNameList.get(i)+ "]"
								+ ProtocolUtils.getMentionProtoUtils(uidList
										.get(i)));
			}
		}
//		Pattern pattern = Pattern.compile("(https?://)?\\w+(\\.\\w+)+(/\\S+)?");
		Pattern pattern = Pattern.compile("(https?://)?[a-zA-Z0-9]*[a-zA-z]+[a-zA-Z0-9]*(\\.\\w+)+(/\\S+)?");
		Matcher matcher = pattern.matcher(temp);
		int offset = 0;
		while (matcher.find()) {  
			String replaceUrl = matcher.group(0);
			StringBuilder sb = new StringBuilder(temp);
			int replaceUrlBeginLocation = sb.indexOf(replaceUrl,offset);
			String matchedUrl = matcher.group(0);
			if (matchedUrl.startsWith("http://")|| matchedUrl.startsWith("https://")) {
		        temp = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0)+ "]" + "(" + matcher.group(0) + ")").toString();
		        offset = replaceUrlBeginLocation + replaceUrl.length()*2 + 4;//4代表两个中小括号
			} else {
		        temp = sb.replace(replaceUrlBeginLocation, replaceUrlBeginLocation + replaceUrl.length(), "[" + matcher.group(0)+ "]" + "(http://" + matcher.group(0) + ")").toString();
		        offset = replaceUrlBeginLocation + replaceUrl.length()*2 + 4 + 7;//4代表两个中小括号,7代表http://
			}
        }
		return temp;
	}

}
