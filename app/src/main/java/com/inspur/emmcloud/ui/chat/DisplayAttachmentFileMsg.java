package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentFile;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

/**
 * DisplayAttachmentFileMsg
 *
 * @author Fortune Yu 展示文件卡片 2016-08-19
 */
public class DisplayAttachmentFileMsg {
    /**
     * 文件卡片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View getView(final Context context,
                               final MsgRobot msg) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_attachment_file_view, null);
        TextView fileNameText = (TextView) convertView
                .findViewById(R.id.file_name_text);
        TextView fileSizeText = (TextView) convertView
                .findViewById(R.id.file_size_text);
        ImageView img = (ImageView)convertView.findViewById(R.id.file_icon_img);
        final MsgContentAttachmentFile msgContentFile = msg.getMsgContentAttachmentFile();
        ImageDisplayUtils.getInstance().displayImage(img, "drawable://" + FileUtils.getIconResId(msgContentFile.getCategory()));
        fileNameText.setText(msgContentFile.getName());
        fileSizeText.setText(FileUtils.formatFileSize(msgContentFile.getSize()));
        return convertView;
    }


}
