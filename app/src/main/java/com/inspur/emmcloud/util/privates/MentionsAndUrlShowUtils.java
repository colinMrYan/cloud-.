package com.inspur.emmcloud.util.privates;

import android.text.SpannableString;
import android.text.Spanned;

import com.inspur.emmcloud.bean.work.MentionsAndUrl;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.widget.spans.URLClickableSpan;

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

			StringBuilder stringBuilder = new StringBuilder();
			char  leftBrackets  = '(';
			for(int i = patternString.length() - 1; i > 0; i--){
				char item = patternString.charAt(i);
				stringBuilder.append(item);
				if(item == leftBrackets){
					break;
				}
			}
			if(stringBuilder.toString().length() > 0){
				protocolResource = stringBuilder.reverse().toString();
				if(stringBuilder.toString().startsWith("(ecm-contact://")){
					protocol = stringBuilder.toString().replace("(", "").replace(")", "");
					protocolResource = stringBuilder.toString().replace("ecm-contact://", "")
							.replace("(", "\"").replace(")", "\"");
				}else {
					protocol = stringBuilder.toString().replace("(", "").replace(")", "");
				}
			}
			//废掉的逻辑
			//修改正则表达式，和IOS一致，注释掉的为原来表达式
//			Pattern patternProtocol = Pattern.compile("\\(.*\\)");
//			Pattern patternProtocol = Pattern.compile("\\((((https?|ec[cm](-[0-9a-z]+)+|gs-msg)://[a-zA-Z0-9\\_\\-]+(\\.[a-zA-Z0-9\\_\\-]+)*(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?)|([a-zA-Z0-9\\-\\_]+\\.)+([a-zA-Z\\-\\_]+)(\\:\\d{2,4})?(/?[a-zA-Z0-9\\-\\_\\.\\?\\=\\&\\%\\#]+)*/?|\\d+(\\.\\d+){3}(\\:\\d{2,4})?)\\)\n");
//			Matcher matcherProtocol = patternProtocol.matcher(patternString);
//			LogUtils.YfcDebug("取出来的文字"+stringBuilder.reverse());
//			while (matcherProtocol.find()) {
//				protocolResource = matcherProtocol.group();
//				if(protocolResource.startsWith("(ecm-contact://")){
//					protocol = protocolResource.replace("(", "").replace(")", "");
//					protocolResource = protocolResource.replace("ecm-contact://", "")
//							.replace("(", "\"").replace(")", "\"");
//				}else {
//					protocol = protocolResource.replace("(", "").replace(")", "");
//				}
//			}
//			LogUtils.YfcDebug("protocol:"+protocol);
//			LogUtils.YfcDebug("protocolResource:"+protocolResource);
			Pattern patternContent = Pattern.compile("\\[.*\\]");
			Matcher matcherContent = patternContent.matcher(patternString);
			while (matcherContent.find()) {
				content = matcherContent.group();
				content = content.replace("[", "").replace("]", "");
//				contentResource = "\""+content+"\"";
				contentResource = content;
				protocolResourceGS = protocolResource.replace("(", "").replace(")", "");
			}

			if(mentionList.contains(protocolResource)){
				hasProtocol = true;
			}
			int urlSize = urlList.size();
			for (int i = 0; i < urlSize; i++) {
				String unesUrl = StringEscapeUtils.unescapeJava(urlList.get(i));
				unesUrl = unesUrl.replaceAll("\"","");
				if(protocolResourceGS.contains(contentResource) || unesUrl.equals(protocolResourceGS)){
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
			String url = mentionsAndUrl.getProtocol();
			URLClickableSpan urlClickableSpan = new URLClickableSpan(url);
			spannableString.setSpan(urlClickableSpan, mentionsAndUrl.getStart(), mentionsAndUrl.getEnd(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
		return spannableString;
	}
}
