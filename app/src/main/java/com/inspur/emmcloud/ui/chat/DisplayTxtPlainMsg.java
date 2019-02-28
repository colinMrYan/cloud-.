package com.inspur.emmcloud.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.widget.LinkMovementClickMethod;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

/**
 * DisplayTxtRichMsg
 *
 * @author sunqx 展示富文本卡片 2016-08-19
 */
public class DisplayTxtPlainMsg {

    /**
     * 富文本卡片
     *
     * @param context
     * @param convertView
     * @param message
     */
    public static View getView(final Context context,
                               Message message) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_rich_view, null);
        final boolean isMyMsg = message.getFromUser().equals(
                MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.bg_other_card));
        cardLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        final TextView contentText = (TextView) cardContentView
                .findViewById(R.id.tv_content);
        contentText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        String text = message.getMsgContentTextPlain().getText();
        contentText.setMovementMethod(LinkMovementClickMethod.getInstance());
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(context, text, message.getMsgContentTextPlain().getMentionsMap());
        contentText.setText(spannableString);
        TransHtmlToTextUtils.stripUnderlines(
                contentText, context.getResources().getColor(isMyMsg ? R.color.hightlight_in_blue_bg
                        : R.color.header_bg));
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
