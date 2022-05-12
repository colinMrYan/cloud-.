package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;

/**
 * DisplayRegularFileMsg
 *
 * @author Fortune Yu 展示文件卡片 2016-08-19
 */
public class DisplayRegularFileMsg {
    /**
     * 文件卡片
     * @param context
     * @param message
     * @param sendStauts
     * @param isMsgDetial
     * @return
     */
    public static View getView(final Context context,
                               final Message message, final int sendStauts, boolean isMsgDetial) {
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_attachment_file_view, null);
        TextView fileNameText = (TextView) convertView
                .findViewById(R.id.tv_file_name);
        TextView fileSizeText = (TextView) convertView
                .findViewById(R.id.tv_file_size);
        BubbleLayout cardLayout = (BubbleLayout) convertView.findViewById(R.id.bl_card);
        if (!isMsgDetial) {
            cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        } else {
            cardLayout.setArrowHeight(0);
            cardLayout.setArrowWidth(0);
            cardLayout.setCornersRadius(0);
        }
        cardLayout.setBubbleColor(context.getResources().getColor(ResourceUtils.getResValueOfAttr(context, R.attr.bubble_bg_color)));
        ImageView img = convertView.findViewById(R.id.iv_file_icon);
        final MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
        img.setImageResource(FileUtils.getFileIconResIdByFileName(msgContentFile.getName()));
        fileNameText.setText(msgContentFile.getName());
        fileSizeText.setText(FileUtils.formatFileSize(msgContentFile.getSize()));
        return convertView;
    }


}
