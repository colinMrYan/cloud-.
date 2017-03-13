package com.inspur.emmcloud.ui.chat;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Comment;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;

public class DisplayCommentMsg {

	/**
	 * 评论详情卡片
	 * 
	 * @param context
	 * @param convertView
	 * @param msg
	 * @param imageDisplayUtils
	 * @param apiService
	 * @param channelType
	 * @param commentList
	 * @param position
	 */
	public static void displayCommentDetailMsgs(final Context context,
			View convertView, final Msg msg,
			 ChatAPIService apiService,
			final String channelType, List<Comment> commentList, int position) {
		TextView commentContent = (TextView) convertView
				.findViewById(R.id.comment_text);
		commentContent.setMovementMethod(LinkMovementMethod.getInstance());
		String	content = commentList.get(position).getSource();
//		String msgBody = msg.getBody();
//		String source = JSONUtils.getString(msgBody, "source", "");
		String[] mentions = JSONUtils.getString(content, "mentions", "").replace("[", "").replace("]", "").split(",");
		String[] urls = JSONUtils.getString(content, "urlList", "").replace("[", "").replace("]", "").split(",");
		List<String> mentionList = Arrays.asList(mentions);
		List<String> urlList = Arrays.asList(urls);
//		String handMention = MentionsMatcher.handleMentioin(source);
		SpannableString spannableString = MentionsAndUrlShowUtils.handleMentioin(content,mentionList,urlList);
//		String handMention = MentionsMatcher.handleMentioin(content);
//		commentContent.setText(Html.fromHtml(handMention));
		commentContent.setText(spannableString);
		TransHtmlToTextUtils.stripUnderlines(commentContent, Color.parseColor("#0f7bca"));
	}

}
