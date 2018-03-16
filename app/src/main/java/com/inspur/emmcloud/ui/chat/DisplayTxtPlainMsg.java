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
import com.inspur.emmcloud.bean.chat.MsgRobot;
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
     * @param msg
     */
    public static View getView(final Context context,
                               MsgRobot msg) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_text_rich_view, null);
        final boolean isMyMsg = msg.getFromUser().equals(
                ((MyApplication) context.getApplicationContext()).getUid());
        final TextView richText = (TextView) convertView
                .findViewById(R.id.content_text);
        (convertView.findViewById(R.id.card_layout)).setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_img_cover_arrow_right : R.drawable.ic_chat_msg_img_cover_arrow_left);
        String text = msg.getMsgContentTextPlain().getText();
        richText.setMovementMethod(LinkMovementClickMethod.getInstance());
        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(context, text, msg.getMsgContentTextPlain().getMentionsMap());
        richText.setText(spannableString);
        TransHtmlToTextUtils.stripUnderlines(
                richText,
                context.getResources().getColor(R.color.header_bg));

        richText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyContentToPasteBoard(context, richText);
                return true;
            }
        });
        return convertView;
    }

    public static void copyContentToPasteBoard(Context context, TextView textView) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(null, textView.getText().toString()));
        ToastUtils.show(context, R.string.copyed_to_paste_board);
    }
}
