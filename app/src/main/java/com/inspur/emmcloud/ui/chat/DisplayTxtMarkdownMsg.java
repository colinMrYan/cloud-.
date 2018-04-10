package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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
        final boolean isMyMsg = msg.getFromUser().equals(
                ((MyApplication) context.getApplicationContext()).getUid());
        final TextView richTitleText = (TextView) cardContentView
                .findViewById(R.id.title_text);
        final TextView richContentText = (TextView) cardContentView
                .findViewById(R.id.content_text);


        (cardContentView
                .findViewById(R.id.root_layout)).setBackgroundResource(isMyMsg?R.drawable.ic_chat_msg_img_cover_arrow_right:R.drawable.ic_chat_msg_img_cover_arrow_left);;

        richTitleText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        richContentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));


        String text = msg.getMsgContentTextMarkdown().getText();
        String title = msg.getMsgContentTextMarkdown().getTitle();
        richTitleText.setVisibility(StringUtils.isBlank(title)?View.GONE:View.VISIBLE);
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
                .into(richTitleText);
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
                .into(richContentText);
        return cardContentView;
    }

}
