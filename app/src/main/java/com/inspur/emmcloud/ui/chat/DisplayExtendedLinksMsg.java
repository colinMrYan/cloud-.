package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

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
        boolean isMyMsg = message.getFromUser().equals(
                MyApplication.getInstance().getUid());
        MsgContentExtendedLinks msgContentExtendedLinks = message.getMsgContentExtendedLinks();
        String title = msgContentExtendedLinks.getTitle();
        String subTitle = msgContentExtendedLinks.getSubtitle();
        String poster = msgContentExtendedLinks.getPoster();
        TextView linkTitleText = (TextView) cardContentView
                .findViewById(R.id.news_card_title_text);
        TextView linkDigestText = (TextView) cardContentView
                .findViewById(R.id.news_card_digest_text);
        linkTitleText.setText(StringUtils.isBlank(title)?context.getString(R.string.share_default_title):title);
        linkDigestText.setText(subTitle);

        ImageView linkImageview = (ImageView) cardContentView
                .findViewById(R.id.news_card_content_img);
        if (!StringUtils.isBlank(poster)) {
            ImageDisplayUtils.getInstance().displayImage(linkImageview, poster, R.drawable.icon_photo_default);
        } else {
            linkImageview.setVisibility(View.GONE);
        }
        int normalPadding = DensityUtil.dip2px(context, 10);
        int arrowPadding = DensityUtil.dip2px(context, 8);
        if (isMyMsg) {
            (cardContentView.findViewById(R.id.text_layout)).setPadding(normalPadding, normalPadding, normalPadding
                    + arrowPadding, normalPadding);
        } else {
            (cardContentView.findViewById(R.id.text_layout)).setPadding(normalPadding + arrowPadding, normalPadding,
                    normalPadding, normalPadding);
        }
        return cardContentView;
    }


}
