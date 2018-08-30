package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.richtext.CacheType;
import com.inspur.emmcloud.util.common.richtext.LinkHolder;
import com.inspur.emmcloud.util.common.richtext.RichText;
import com.inspur.emmcloud.util.common.richtext.RichType;
import com.inspur.emmcloud.util.common.richtext.callback.LinkFixCallback;
import com.inspur.emmcloud.util.common.richtext.callback.OnUrlClickListener;
import com.inspur.emmcloud.util.common.richtext.callback.OnUrlLongClickListener;
import com.inspur.emmcloud.util.privates.CustomProtocol;
import com.inspur.emmcloud.util.privates.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.widget.LinkMovementClickMethod;

import java.util.Arrays;
import java.util.List;

/**
 * DisplayTxtRichMsg
 *
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class DisplayTxtRichMsg {

    /**
     * 富文本卡片
     *
     * @param context
     * @param msg
     */
    public static View displayRichTextMsg(final Context context,
                                          Msg msg) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_rich_view, null);
        final boolean isMyMsg = msg.getUid().equals(
                ((MyApplication) context.getApplicationContext()).getUid());
        final TextView richText = (TextView) cardContentView
                .findViewById(R.id.content_text);
        richText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        (cardContentView.findViewById(R.id.card_layout)).setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_img_cover_arrow_right : R.drawable.ic_chat_msg_img_cover_arrow_left);
        String msgBody = msg.getBody();
        String source = JSONUtils.getString(msgBody, "source", "");
        if (MyAppConfig.isUseMarkdown) {
            RichText.from(source)
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
                            }else if(url.startsWith("ecc-cmd")){
                                CustomProtocol customProtocol = new CustomProtocol(url);
                            }
                            return false;
                        }
                    })
                    .urlLongClick(new OnUrlLongClickListener() {
                        @Override
                        public boolean urlLongClick(String url) {
                            copyContentToPasteBoard(context, richText);
                            return true;
                        }
                    })
                    .noImage(true)
                    .singleLoad(false)
                    .cache(CacheType.ALL)
                    .into(richText);
        } else {
            richText.setMovementMethod(LinkMovementClickMethod.getInstance());
            String[] mentions = JSONUtils.getString(msgBody, "mentions", "")
                    .replace("[", "").replace("]", "").split(",");
            String[] urls = JSONUtils.getString(msgBody, "urls", "")
                    .replace("[", "").replace("]", "").split(",");
            List<String> mentionList = Arrays.asList(mentions);
            List<String> urlList = Arrays.asList(urls);
            SpannableString spannableString = MentionsAndUrlShowUtils
                    .handleMentioin(source, mentionList, urlList);
            richText.setText(spannableString);
            TransHtmlToTextUtils.stripUnderlines(
                    richText,
                    context.getResources().getColor(
                            isMyMsg ? R.color.hightlight_in_blue_bg
                                    : R.color.header_bg));

        }
        richText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyContentToPasteBoard(context, richText);
                return true;
            }
        });
        return cardContentView;
    }

    public static void copyContentToPasteBoard(Context context, TextView textView) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(null, textView.getText().toString()));
        ToastUtils.show(context, R.string.copyed_to_paste_board);
    }
}
