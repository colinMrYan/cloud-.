package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

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
     * @param msg
     * @param isShowCommentBtn
     */
    public static View displayResLinkMsg(final Activity context,
                                         final Msg msg, boolean isShowCommentBtn) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_link_view, null);
        boolean isMyMsg = msg.getUid().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        String msgBody = msg.getBody();
        String linkTitle = JSONUtils.getString(msgBody, "title", "");
        String linkDigest = JSONUtils.getString(msgBody, "digest", "");
        String linkPoster = JSONUtils.getString(msgBody, "poster", "");
        TextView linkTitleText = (TextView) cardContentView
                .findViewById(R.id.tv_news_card_title);
        TextView linkDigestText = (TextView) cardContentView
                .findViewById(R.id.tv_news_card_digest);
        linkTitle = StringUtils.isBlank(linkTitle) ? context.getString(R.string.share_default_title) : linkTitle;
        linkTitleText.setText(linkTitle);
        linkDigestText.setText(linkDigest);
        if (StringUtils.isBlank(linkTitle)) {
            linkTitleText.setVisibility(View.GONE);
        }
        if (StringUtils.isBlank(linkDigest)) {
            linkDigestText.setVisibility(View.GONE);
        }
        ImageView linkImageview = (ImageView) cardContentView
                .findViewById(R.id.img_news_card);
        if (!StringUtils.isBlank(linkPoster)) {
            ImageDisplayUtils.getInstance().displayImage(linkImageview, APIUri.getPreviewUrl(linkPoster), R.drawable.icon_photo_default);
        } else {
            linkImageview.setVisibility(View.GONE);
        }
        if (context instanceof ChannelV0Activity) {
            int normalPadding = DensityUtil.dip2px(context, 10);
            int arrowPadding = DensityUtil.dip2px(context, 8);
            if (isMyMsg) {
                (cardContentView.findViewById(R.id.rl_text)).setPadding(normalPadding, normalPadding, normalPadding
                        + arrowPadding, normalPadding);
            } else {
                (cardContentView.findViewById(R.id.rl_text)).setPadding(normalPadding + arrowPadding, normalPadding,
                        normalPadding, normalPadding);
            }
        }
        return cardContentView;

    }

    /**
     * 展示链接类卡片，如新闻
     *
     * @param context
     * @param msg
     */
    public static View displayResLinkMsg(Activity context,
                                         Msg msg) {
        return displayResLinkMsg(context, msg, true);
    }

}
