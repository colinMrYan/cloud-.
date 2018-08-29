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
        final TextView richText = (TextView) cardContentView
                .findViewById(R.id.content_text);
        richText.setTextColor(context.getResources().getColor(
                isMyMsg ? R.color.white : R.color.black));
        (cardContentView.findViewById(R.id.card_layout)).setBackgroundColor(context.getResources().getColor(
                isMyMsg ? R.color.bg_my_card : R.color.white));

        (cardContentView.findViewById(R.id.text_layout)).setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_img_cover_arrow_right : R.drawable.ic_chat_msg_img_cover_arrow_left);
        String text = message.getMsgContentTextPlain().getText();
        richText.setMovementMethod(LinkMovementClickMethod.getInstance());
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(context, text, message.getMsgContentTextPlain().getMentionsMap());
        richText.setText(spannableString);
        TransHtmlToTextUtils.stripUnderlines(
                richText,context.getResources().getColor(isMyMsg ? R.color.hightlight_in_blue_bg
                        : R.color.header_bg));
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
