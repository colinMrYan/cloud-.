package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DisplayTxtRichMsg
 * 
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class DisplayTxtRichMsg {

	/**
	 * 富文本卡片
	 * 
	 * @param context
	 * @param convertView
	 * @param msg
	 */
	public static void displayRichTextMsg(Context context, View convertView,
			Msg msg) {
		boolean isMyMsg = msg.getUid().equals(
				((MyApplication) context.getApplicationContext()).getUid());
		TextView richText = (TextView) convertView
				.findViewById(R.id.content_text);
		richText.setMovementMethod(LinkMovementMethod.getInstance());
		String msgBody = msg.getBody();
		String source = JSONUtils.getString(msgBody, "source", "");
		String[] mentions = JSONUtils.getString(msgBody, "mentions", "")
				.replace("[", "").replace("]", "").split(",");
		String[] urls = JSONUtils.getString(msgBody, "urls", "")
				.replace("[", "").replace("]", "").split(",");
		List<String> mentionList = Arrays.asList(mentions);
		List<String> urlList = Arrays.asList(urls);
		SpannableString spannableString = MentionsAndUrlShowUtils
				.handleMentioin(source, mentionList, urlList);
		richText.setText(spannableString);
		richText.setBackgroundColor(context.getResources().getColor(
				isMyMsg ? R.color.header_bg : R.color.white));
		richText.setTextColor(context.getResources().getColor(
				isMyMsg ? R.color.white : R.color.black));
		int normalPadding = DensityUtil.dip2px(context, 10);
		int arrowPadding = DensityUtil.dip2px(context, 8);
		if (isMyMsg) {
			richText.setPadding(normalPadding, normalPadding, normalPadding+arrowPadding, normalPadding);
		}else {
			richText.setPadding(normalPadding+arrowPadding, normalPadding,normalPadding , normalPadding);
		}
		TransHtmlToTextUtils.stripUnderlines(
				richText,
				context.getResources().getColor(
						isMyMsg ? R.color.hightlight_in_blue_bg
								: R.color.header_bg));
	}

	/**
	 * 半角转换为全角
	 * 
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {// 全角空格为12288，半角空格为32
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)// 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	/**
	 * 去除特殊字符或将所有中文标号替换为英文标号
	 * 
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		str = str.replaceAll("【", "[").replaceAll("】", "]")
				.replaceAll("！", "!").replaceAll("：", ":");// 替换中文标号
		String regEx = "[『』]"; // 清除掉特殊字符
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

}
