package com.inspur.emmcloud.ui.chat;

import java.io.Serializable;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.ui.app.groupnews.NewsWebDetailActivity;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONObjectUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;

/**
 * DisplayResLinkMsg
 * 
 * @author sunqx 展示链接卡片 2016-08-19
 */
public class DisplayResLinkMsg {

	/**
	 * 展示链接类卡片，如新闻
	 *
	 * @param context
	 * @param convertView
	 * @param msg
	 * @param imageDisplayUtils
	 * @param channelType
	 * @param isShowCommentBtn
	 *            是否显示评论按钮，消息详情页面不需要显示
	 */
	public static void displayResLinkMsg(final Activity context,
			View convertView, final Msg msg, boolean isShowCommentBtn) {
		boolean isMyMsg = msg.getUid().equals(
				((MyApplication) context.getApplicationContext()).getUid());
		String msgBody = msg.getBody();
		final String linkTitle = JSONUtils.getString(msgBody, "title", "");
		final String linkDigest = JSONUtils.getString(msgBody, "digest", "");
		final String linkUrl = JSONUtils.getString(msgBody, "url", "");
		final String linkPoster = JSONUtils.getString(msgBody, "poster", "");
		TextView linkTitleText = (TextView) convertView
				.findViewById(R.id.news_card_title_text);
		TextView linkDigestText = (TextView) convertView
				.findViewById(R.id.news_card_digest_text);
		linkTitleText.setText(linkTitle);
		linkDigestText.setText(linkDigest);

		ImageView linkImageview = (ImageView) convertView
				.findViewById(R.id.news_card_content_img);
		if (!StringUtils.isBlank(linkPoster)) {
			new ImageDisplayUtils(context, R.drawable.icon_photo_default)
					.display(linkImageview, UriUtils.getPreviewUri(linkPoster));
		} else {
			linkImageview.setVisibility(View.GONE);
		}
		((RelativeLayout) convertView.findViewById(R.id.news_card_layout))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						bundle.putString("url", linkUrl);
						bundle.putString("title", linkTitle);
						bundle.putString("digest", linkDigest);
						bundle.putString("poster", linkPoster);
						bundle.putBoolean("tran", true);
						IntentUtils.startActivity(context,
								NewsWebDetailActivity.class, bundle);
					}
				});
		if (isShowCommentBtn) {
			((RelativeLayout) convertView.findViewById(R.id.news_card_layout))
					.setBackgroundResource(isMyMsg ? R.drawable.shape_chat_msg_card_my
							: R.drawable.shape_chat_msg_card_other);
//			linkTitleText.setTextColor(context.getResources().getColor(
//					isMyMsg ? R.color.white : R.color.black));
//			linkDigestText.setTextColor(context.getResources().getColor(
//					isMyMsg ? R.color.white : R.color.text_grey));

		}
	}

	/**
	 * 展示链接类卡片，如新闻
	 *
	 * @param context
	 * @param convertView
	 * @param msg
	 * @param imageDisplayUtils
	 * @param channelType
	 */
	public static void displayResLinkMsg(Activity context, View convertView,
			Msg msg) {
		displayResLinkMsg(context, convertView, msg, true);
	}

}
