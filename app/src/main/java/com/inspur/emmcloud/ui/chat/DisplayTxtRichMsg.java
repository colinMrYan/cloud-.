package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.richtext.CacheType;
import com.inspur.emmcloud.util.common.richtext.LinkHolder;
import com.inspur.emmcloud.util.common.richtext.RichText;
import com.inspur.emmcloud.util.common.richtext.RichType;
import com.inspur.emmcloud.util.common.richtext.callback.LinkFixCallback;
import com.inspur.emmcloud.util.common.richtext.callback.OnUrlClickListener;
import com.inspur.emmcloud.util.common.richtext.callback.OnUrlLongClickListener;
import com.inspur.emmcloud.util.privates.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.widget.LinkMovementClickMethod;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

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
    public static View displayRichTextMsg(final Context context, Msg msg) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_rich_view, null);
        final boolean isMyMsg = msg.getUid().equals(MyApplication.getInstance().getUid());
        final TextView contentText = (TextView) cardContentView
                .findViewById(R.id.tv_content);
        contentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.bg_other_card));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        String msgBody = msg.getBody();
        String source = JSONUtils.getString(msgBody, "source", "");
        if (msg.getUid().toLowerCase().startsWith("bot")) {
            RichText.from(source)
                    .type(RichType.markdown)
                    .linkFix(new LinkFixCallback() {
                        @Override
                        public void fix(LinkHolder holder) {
                            holder.setUnderLine(false);
                            holder.setColor(context.getResources().getColor(
                                    isMyMsg ? R.color.hightlight_in_blue_bg
                                            : R.color.header_bg_blue));
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
                    .urlLongClick(new OnUrlLongClickListener() {
                        @Override
                        public boolean urlLongClick(String url) {
                            copyContentToPasteBoard(context, contentText);
                            return true;
                        }
                    })
                    .noImage(true)
                    .singleLoad(false)
                    .cache(CacheType.all)
                    .into(contentText);
        } else {
            contentText.setMovementMethod(LinkMovementClickMethod.getInstance());
            SpannableString spannableString = MentionsAndUrlShowUtils
                    .getMsgContentSpannableString(msgBody);
            contentText.setText(spannableString);
            TransHtmlToTextUtils.stripUnderlines(
                    contentText,
                    context.getResources().getColor(
                            isMyMsg ? R.color.hightlight_in_blue_bg
                                    : R.color.header_bg_blue));

        }
        contentText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyContentToPasteBoard(context, contentText);
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
