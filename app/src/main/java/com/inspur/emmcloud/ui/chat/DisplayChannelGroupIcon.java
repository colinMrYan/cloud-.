package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.CircleFrameLayout;

import java.util.List;

/**
 * Created by Administrator on 2017/5/13.
 */

public class DisplayChannelGroupIcon {
    public static void show(Context context, String cid, CircleFrameLayout channelPhotoLayout) {

        int defaultIcon = R.drawable.icon_person_default;

        List<String> memberUidList = ChannelGroupCacheUtils.getMemberUidList(context, cid, 4);

        int groupNumSize = memberUidList.size();
        if (groupNumSize > 0) {
            View channelPhotoView = null;
            ImageView photoImg1 = null;
            ImageView photoImg2 = null;
            ImageView photoImg3 = null;
            ImageView photoImg4 = null;
            switch (groupNumSize) {
                case 1:
                    channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_one, null);
                    ImageView photoImg = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg, UriUtils.getChannelImgUri(context, memberUidList.get(0)));
                    break;
                case 2:
                    channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_two, null);
                    photoImg1 = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
                    photoImg2 = (ImageView) channelPhotoView.findViewById(R.id.photo_img2);
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg1, UriUtils.getChannelImgUri(context, memberUidList.get(0)));
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg2, UriUtils.getChannelImgUri(context, memberUidList.get(1)));
                    break;
                case 3:
                    channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_three, null);
                    photoImg1 = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
                    photoImg2 = (ImageView) channelPhotoView.findViewById(R.id.photo_img2);
                    photoImg3 = (ImageView) channelPhotoView.findViewById(R.id.photo_img3);
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg1, UriUtils.getChannelImgUri(context, memberUidList.get(0)));
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg2, UriUtils.getChannelImgUri(context, memberUidList.get(1)));
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg3, UriUtils.getChannelImgUri(context, memberUidList.get(2)));
                    break;
                default:
                    channelPhotoView = LayoutInflater.from(context).inflate(R.layout.chat_msg_session_photo_four, null);
                    photoImg1 = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
                    photoImg2 = (ImageView) channelPhotoView.findViewById(R.id.photo_img2);
                    photoImg3 = (ImageView) channelPhotoView.findViewById(R.id.photo_img3);
                    photoImg4 = (ImageView) channelPhotoView.findViewById(R.id.photo_img4);
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg1, UriUtils.getChannelImgUri(context, memberUidList.get(0)));
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg2, UriUtils.getChannelImgUri(context, memberUidList.get(1)));
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg3, UriUtils.getChannelImgUri(context, memberUidList.get(2)));
                    new ImageDisplayUtils(context, defaultIcon).display(
                            photoImg4, UriUtils.getChannelImgUri(context, memberUidList.get(3)));
                    break;
            }
            channelPhotoLayout.addView(channelPhotoView);
        } else {
            channelPhotoLayout.setBackgroundResource(R.drawable.icon_channel_group_default);
        }

    }


}
