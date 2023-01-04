package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.chat.Email;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentCard;

import java.util.List;

/**
 * DisplayAttachmentCardMsg
 *
 * @author sunqx 展示名片 2016-08-19
 */
public class Design3DisplayAttachmentCardMsg {

    /**
     * 名片
     *
     * @param context
     * @param message
     */
    public static View getView(final Context context,
                               Message message) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.design3_chat_msg_card_child_attachment_card_view, null);
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout bubbleLayout = (BubbleLayout) convertView.findViewById(R.id.bl_card);
        bubbleLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        bubbleLayout.setStrokeWidth(0);
        ImageView cardPhotoImg = (ImageView) convertView.findViewById(R.id.img_photo);
        TextView cardNameText = (TextView) convertView.findViewById(R.id.tv_name);
        TextView cardEmailText = (TextView) convertView.findViewById(R.id.tv_mail);
        MsgContentAttachmentCard msgContentCard = message.getMsgContentAttachmentCard();
        ImageDisplayUtils.getInstance().displayImage(cardPhotoImg, msgContentCard.getAvatar(), R.drawable.icon_person_default);
        cardNameText.setText(msgContentCard.getFirstName() + msgContentCard.getLastName());
        List<Email> emailList = msgContentCard.getEmailList();
        if (emailList.size() > 0) {
            cardEmailText.setText(emailList.get(0).getAddress());
        }
        return convertView;
    }

}
