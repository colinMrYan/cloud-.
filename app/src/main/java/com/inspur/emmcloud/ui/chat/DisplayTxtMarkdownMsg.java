package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cpiz.android.bubbleview.BubbleRelativeLayout;
import com.cpiz.android.bubbleview.BubbleStyle;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.richtext.CacheType;
import com.inspur.emmcloud.util.common.richtext.LinkHolder;
import com.inspur.emmcloud.util.common.richtext.RichText;
import com.inspur.emmcloud.util.common.richtext.RichType;
import com.inspur.emmcloud.util.common.richtext.callback.LinkFixCallback;
import com.inspur.emmcloud.util.common.richtext.callback.OnUrlClickListener;
import com.inspur.emmcloud.util.privates.UriUtils;

/**
 * DisplayTxtRichMsg
 *
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class DisplayTxtMarkdownMsg {

    /**
     * 富文本卡片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View getView(final Context context, Message msg) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_markdown_view, null);
        final boolean isMyMsg = msg.getFromUser().equals(MyApplication.getInstance().getUid());
        final TextView titleText = (TextView) cardContentView
                .findViewById(R.id.tv_title);
        final TextView contentText = (TextView) cardContentView
                .findViewById(R.id.tv_content);
        BubbleRelativeLayout cardLayout = (BubbleRelativeLayout)cardContentView.findViewById(R.id.brl_card);
        cardLayout.setArrowDirection(isMyMsg? BubbleStyle.ArrowDirection.Right:BubbleStyle.ArrowDirection.Left);
        cardLayout.setFillColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.white));
        titleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        contentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        String text = msg.getMsgContentTextMarkdown().getText();
        String title = msg.getMsgContentTextMarkdown().getTitle();
        titleText.setVisibility(StringUtils.isBlank(title)?View.GONE:View.VISIBLE);
        RichText.from(title)
                .type(RichType.MARKDOWN)
                .linkFix(new LinkFixCallback() {
                    @Override
                    public void fix(LinkHolder holder) {
                        holder.setUnderLine(false);
                        holder.setColor(context.getResources().getColor(
                                isMyMsg ? R.color.hightlight_in_blue_bg
                                        : R.color.header_bg));
                    }
                })
                .urlClick(new OnUrlClickListener() {
                    @Override
                    public boolean urlClicked(String url) {
                        if (url.startsWith("http")) {
                            UriUtils.openUrl((Activity) context, url);
                            return true;
                        }
                        return false;
                    }
                })
                .noImage(true)
                .singleLoad(false)
                .cache(CacheType.ALL)
                .into(titleText);



        RichText.from(text)
                .type(RichType.MARKDOWN)
                .linkFix(new LinkFixCallback() {
                    @Override
                    public void fix(LinkHolder holder) {
                        holder.setUnderLine(false);
                        holder.setColor(context.getResources().getColor(
                                isMyMsg ? R.color.hightlight_in_blue_bg
                                        : R.color.header_bg));
                    }
                })
                .urlClick(new OnUrlClickListener() {
                    @Override
                    public boolean urlClicked(String url) {
                        if (url.startsWith("http")) {
                            UriUtils.openUrl((Activity) context, url);
                            return true;
                        }
                        return false;
                    }
                })
                .noImage(true)
                .singleLoad(false)
                .cache(CacheType.ALL)
                .into(contentText);
        return cardContentView;
    }

}
