package com.inspur.emmcloud.util;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.inspur.emmcloud.bean.MentionsAndUrl;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionsAndUrlShowUtils {

	/**
	 * 把mentions和urlList的信息组合成可渲染的字符串
	 * @param mentions
	 * @param mentionList
	 * @param urlList
     * @return
     */
	public static SpannableString handleMentioin(String mentions,List<String> mentionList,List<String> urlList) {
		ArrayList<MentionsAndUrl> mentionsAndUrls = new ArrayList<MentionsAndUrl>();
	    Pattern pattern = Pattern.compile("\\[[^\\]]+\\]\\([^\\)]+\\)");
	    if(StringUtils.isBlank(mentions)){
	    	return new SpannableString("");
	    }
	    Matcher matcher = pattern.matcher(mentions);
	    while (matcher.find()) {
			String patternString = matcher.group();
			String protocolResource = "";
			String content = "";
			String contentResource = "";
			String protocolResourceGS = "";
			String protocol = "";
			int index = -1;
			index = mentions.indexOf(patternString);
			boolean hasProtocol = false;
			Pattern patternProtocol = Pattern.compile("\\(.*\\)");
			Matcher matcherProtocol = patternProtocol.matcher(patternString);
			while (matcherProtocol.find()) {
				protocolResource = matcherProtocol.group();
				if(protocolResource.startsWith("(ecm-contact://")){
					protocol = protocolResource.replace("(", "").replace(")", "");
					protocolResource = protocolResource.replace("ecm-contact://", "")
							.replace("(", "\"").replace(")", "\"");
				}else {
					protocol = protocolResource.replace("(", "").replace(")", "");
				}
			}
			Pattern patternContent = Pattern.compile("\\[.*\\]");
			Matcher matcherContent = patternContent.matcher(patternString);
			while (matcherContent.find()) {
				content = matcherContent.group();
				content = content.replace("[", "").replace("]", "");
				contentResource = "\""+content+"\"";
				protocolResourceGS = protocolResource.replace("(", "").replace(")", "");
			}

			if(mentionList.contains(protocolResource)){
				hasProtocol = true;
			}
			int urlSize = urlList.size();
			for (int i = 0; i < urlSize; i++) {
				String unesUrl = StringEscapeUtils.unescapeJava(urlList.get(i));
				unesUrl = unesUrl.replace("\"","");
				if(unesUrl.equals(contentResource) || unesUrl.equals(protocolResourceGS)){
					hasProtocol = true;
				}
			}
			if(hasProtocol){
//				mentions = mentions.replace(patternString, content);
				StringBuilder sb = new StringBuilder(mentions);
				mentions = sb.replace(index, index+patternString.length(), content).toString();
				int start = mentions.indexOf(content,index);
				int end = start + content.length();
				MentionsAndUrl mentionsAndUrl = new MentionsAndUrl(start, end, protocol);
				mentionsAndUrls.add(mentionsAndUrl);
			}
		}
	    SpannableString spannableString = new SpannableString(mentions);
	    for (int i = 0; i < mentionsAndUrls.size(); i++) {
	    	MentionsAndUrl mentionsAndUrl = mentionsAndUrls.get(i);
	    	URLSpan urlSpan = new URLSpan(mentionsAndUrl.getProtocol());
	    	int start = mentionsAndUrl.getStart();
	    	int end = mentionsAndUrl.getEnd();
			spannableString.setSpan(urlSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
		return spannableString;
	}
}
