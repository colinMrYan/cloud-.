package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.CircleFrameLayout;

import java.util.List;

/**
 * Created by Administrator on 2017/5/13.
 */

public class DisplayChannelGroupIcon {
	public static void show(Context context, String cid,CircleFrameLayout channelPhotoLayout){
		int defaultIcon = R.drawable.icon_person_default;
		View channelPhotoView = null;
		ChannelGroup channelGroup = ChannelGroupCacheUtils.getChannelGroupById(context, cid);
		if (channelGroup != null) {
			List<String> memberUidList = ChannelGroupCacheUtils.getMemberUidList(context, cid, 4);
			int groupNumSize = memberUidList.size();
			if (groupNumSize == 1) {
				channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_one, null);
				ImageView photoImg = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg, UriUtils.getChannelImgUri(memberUidList.get(0)));
			} else if (groupNumSize == 2) {
				channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_two, null);
				ImageView photoImg1 = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
				ImageView photoImg2 = (ImageView) channelPhotoView.findViewById(R.id.photo_img2);
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg1, UriUtils.getChannelImgUri(memberUidList.get(0)));
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg2, UriUtils.getChannelImgUri(memberUidList.get(1)));
			} else if (groupNumSize == 3) {
				channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_three, null);
				ImageView photoImg1 = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
				ImageView photoImg2 = (ImageView) channelPhotoView.findViewById(R.id.photo_img2);
				ImageView photoImg3 = (ImageView) channelPhotoView.findViewById(R.id.photo_img3);
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg1, UriUtils.getChannelImgUri(memberUidList.get(0)));
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg2, UriUtils.getChannelImgUri(memberUidList.get(1)));
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg3, UriUtils.getChannelImgUri(memberUidList.get(2)));
			} else if (groupNumSize == 4) {
				channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_four, null);
				ImageView photoImg1 = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
				ImageView photoImg2 = (ImageView) channelPhotoView.findViewById(R.id.photo_img2);
				ImageView photoImg3 = (ImageView) channelPhotoView.findViewById(R.id.photo_img3);
				ImageView photoImg4 = (ImageView) channelPhotoView.findViewById(R.id.photo_img4);
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg1, UriUtils.getChannelImgUri(memberUidList.get(0)));
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg2, UriUtils.getChannelImgUri(memberUidList.get(1)));
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg3, UriUtils.getChannelImgUri(memberUidList.get(2)));
				new ImageDisplayUtils(context, defaultIcon).display(
						photoImg4, UriUtils.getChannelImgUri(memberUidList.get(3)));
			}
			if (channelPhotoView != null){
				channelPhotoLayout.addView(channelPhotoView);
			}
		}else {
			channelPhotoLayout.setBackgroundResource(R.drawable.icon_channel_group_default);
		}
	}

	
}
