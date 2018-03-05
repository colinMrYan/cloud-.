package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Email;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentCard;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

import java.util.List;

/**
 * DisplayAttachmentCardMsg
 *
 * @author sunqx 展示名片 2016-08-19
 */
public class DisplayAttachmentCardMsg {

    /**
     * 名片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View getView(final Context context,
                               MsgRobot msg) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_attachment_card_view, null);
        ImageView cardPhotoImg = (ImageView) convertView.findViewById(R.id.photo_img);
        TextView cardNameText = (TextView) convertView.findViewById(R.id.name_text);
        TextView cardEmailText = (TextView) convertView.findViewById(R.id.mail_text);
        MsgContentAttachmentCard msgContentCard = msg.getMsgContentAttachmentCard();
        ImageDisplayUtils.getInstance().displayImage(cardPhotoImg,msgContentCard.getAvatar(), R.drawable.icon_person_default);
        cardNameText.setText(msgContentCard.getFirstName()+msgContentCard.getLastName());
       List<Email> emailList = msgContentCard.getEmailList();
       if (emailList.size()>0){
           cardEmailText.setText(emailList.get(0).getAddress());
       }
       return convertView;
    }

}
