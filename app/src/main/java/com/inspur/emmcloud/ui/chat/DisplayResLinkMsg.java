package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.ImageDisplayUtils;
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
	 * @param context
	 * @param convertView
	 * @param msg
	 * @param isShowCommentBtn
     */
	public static void displayResLinkMsg(final Activity context,
			View convertView, final Msg msg, boolean isShowCommentBtn) {
		boolean isMyMsg = msg.getUid().equals(
				((MyApplication) context.getApplicationContext()).getUid());
		String msgBody = msg.getBody();
		 String linkTitle = JSONUtils.getString(msgBody, "title", "");
		 String linkDigest = JSONUtils.getString(msgBody, "digest", "");
		 String linkPoster = JSONUtils.getString(msgBody, "poster", "");
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
		if (context instanceof ChannelActivity) {
			int normalPadding = DensityUtil.dip2px(context, 10);
			int arrowPadding = DensityUtil.dip2px(context, 8);
			if (isMyMsg) {
				((RelativeLayout) convertView.findViewById(R.id.text_layout)).setPadding(normalPadding, normalPadding, normalPadding
						+ arrowPadding, normalPadding);
			} else {
				((RelativeLayout) convertView.findViewById(R.id.text_layout)).setPadding(normalPadding + arrowPadding, normalPadding,
						normalPadding, normalPadding);
			}
		}
		
	}

	/**
	 * 展示链接类卡片，如新闻
	 *
	 * @param context
	 * @param convertView
	 * @param msg
	 */
	public static void displayResLinkMsg(Activity context, View convertView,
			Msg msg) {
		displayResLinkMsg(context, convertView, msg, true);
	}

}
