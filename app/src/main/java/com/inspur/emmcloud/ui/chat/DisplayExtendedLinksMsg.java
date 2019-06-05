package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

/**
 * DisplayTxtRichMsg
 *
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class DisplayExtendedLinksMsg {

    /**
     * 富文本卡片
     *
     * @param context
     * @param message
     */
    public static View getView(final Context context,
                               Message message) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_link_view, null);
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        MsgContentExtendedLinks msgContentExtendedLinks = message.getMsgContentExtendedLinks();
        String title = msgContentExtendedLinks.getTitle();
        String subTitle = msgContentExtendedLinks.getSubtitle();
        String poster = msgContentExtendedLinks.getPoster();
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

        if (!StringUtils.isBlank(subTitle)) {
            linkDigestText.setText(subTitle);
        } else {
            linkDigestText.setVisibility(View.GONE);
        }
        return cardContentView;
    }


}
