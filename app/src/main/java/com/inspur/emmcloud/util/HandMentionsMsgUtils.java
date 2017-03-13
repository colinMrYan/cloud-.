package com.inspur.emmcloud.util;

import java.util.Arrays;
import java.util.List;

import android.text.SpannableString;

import com.inspur.emmcloud.bean.Msg;

public class HandMentionsMsgUtils {

	/**
	 * rich_text文本获取方法
	 * @param msg
	 * @return 富文本需要显示的内容
	 */
	public static String getRichText(Msg msg){
//		JSONObject richJson = null;
		String rawStr = "";
//		String richtext = "";
//		try {
//			richJson = new JSONObject(msg.getBody());
//			rawStr = richJson.getString("source");
			String msgBody = msg.getBody();
			String source = JSONUtils.getString(msgBody, "source", "");
			String[] mentions = JSONUtils.getString(msgBody, "mentions", "").replace("[", "").replace("]", "").split(",");
			String[] urls = JSONUtils.getString(msgBody, "urlList", "").replace("[", "").replace("]", "").split(",");
			List<String> mentionList = Arrays.asList(mentions);
			List<String> urlList = Arrays.asList(urls);
			SpannableString spannableString = MentionsAndUrlShowUtils.handleMentioin(source,mentionList,urlList);
//			richtext = MentionsMatcher.handleMentioin(rawStr);
			if(spannableString != null){
				return spannableString.toString();
			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		return "";
	}
}
