package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;

/**
 * DisplayTxtRichMsg
 *
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class Design3DisplayExtendedLinksMsg {

    /**
     * 富文本卡片
     *
     * @param context
     * @param message
     */
    public static View getView(final Context context,
                               Message message) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.design3_chat_msg_card_child_res_link_view, null);
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setStrokeWidth(0);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_ne15)));
        final MsgContentExtendedLinks msgContentExtendedLinks = message.getMsgContentExtendedLinks();
        String title = msgContentExtendedLinks.getTitle();
        String subTitle = msgContentExtendedLinks.getSubtitle();
        subTitle = StringUtils.isBlank(subTitle) ? title : subTitle;
        String poster = msgContentExtendedLinks.getPoster();
        LinearLayout extendLayout = (LinearLayout) cardContentView
                .findViewById(R.id.ll_extend_app);
        View extendView = (View) cardContentView
                .findViewById(R.id.view_extend);
        ImageViewRound extendIv = (ImageViewRound) cardContentView
                .findViewById(R.id.iv_extend_image);
        TextView extendTv = (TextView) cardContentView
                .findViewById(R.id.tv_extend_content);
        TextView linkTitleText = (TextView) cardContentView
                .findViewById(R.id.tv_news_card_title);
        TextView linkDigestText = (TextView) cardContentView
                .findViewById(R.id.tv_news_card_digest);
        linkTitleText.setText(StringUtils.isBlank(title) ? context.getString(R.string.share_default_title) : title);
        ImageView linkImg = (ImageView) cardContentView
                .findViewById(R.id.img_news_card);
        if (!StringUtils.isBlank(poster)) {
            ImageDisplayUtils.getInstance().displayImage(linkImg, poster, R.drawable.icon_photo_default);
        } else {
            linkImg.setVisibility(View.GONE);
        }
        linkDigestText.setText(subTitle);
        extendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Constant.APP_WEB_URI, msgContentExtendedLinks.getAppUrl());
                bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, msgContentExtendedLinks.isHaveAPPNavBar());
                bundle.putString(Constant.WEB_FRAGMENT_APP_NAME, msgContentExtendedLinks.getAppName());
                ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
            }
        });
        String appName = msgContentExtendedLinks.getAppName();
        if (TextUtils.isEmpty(appName)) {
            extendLayout.setVisibility(View.GONE);
            extendView.setVisibility(View.GONE);
        } else {
            extendLayout.setVisibility(View.VISIBLE);
            extendView.setVisibility(View.VISIBLE);
            extendTv.setText(appName);
            extendIv.setType(ImageViewRound.TYPE_ROUND);
            extendIv.setRoundRadius(DensityUtil.dip2px(context, 4));
            ImageDisplayUtils.getInstance().displayImage(extendIv, msgContentExtendedLinks.getIco(), R.drawable.ic_app_default);
        }
        return cardContentView;
    }


}
