package com.inspur.emmcloud.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.ui.chat.DisplayAttachmentCardMsg;
import com.inspur.emmcloud.ui.chat.DisplayCommentTextPlainMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedActionsMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedLinksMsg;
import com.inspur.emmcloud.ui.chat.DisplayMediaImageMsg;
import com.inspur.emmcloud.ui.chat.DisplayMediaVoiceMsg;
import com.inspur.emmcloud.ui.chat.DisplayRegularFileMsg;
import com.inspur.emmcloud.ui.chat.DisplayResUnknownMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtMarkdownMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtPlainMsg;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/10.
 */

public class ChannelMessageAdapter extends RecyclerView.Adapter<ChannelMessageAdapter.ViewHolder> {
    private Activity context;
    private List<UIMessage> UIMessageList = new ArrayList<>();
    private MyItemClickListener mItemClickListener;
    private String channelType;
    private ECMChatInputMenu chatInputMenu;

    public ChannelMessageAdapter(Activity context, String channelType, ECMChatInputMenu chatInputMenu) {
        this.context = context;
        this.channelType = channelType;
        this.chatInputMenu = chatInputMenu;
    }

    public void setMessageList(List<UIMessage> UImessageList) {
        this.UIMessageList.clear();
        this.UIMessageList.addAll(UImessageList);
    }

    public void setChannelData(String channelType, ECMChatInputMenu chatInputMenu){
        this.channelType = channelType;
        this.chatInputMenu = chatInputMenu;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_msg_card_parent_view, viewGroup, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UIMessage uimessage = UIMessageList.get(position);
        showCardLayout(holder, uimessage);
        showUserName(holder, uimessage);
        showMsgSendTime(holder, uimessage, position);
        showUserPhoto(holder, uimessage);
        showRefreshingImg(holder, uimessage);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }

    @Override
    public int getItemCount() {
        return UIMessageList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener mListener;
        public RelativeLayout cardLayout;
        public TextView senderNameText;
        public ImageView senderPhotoImgLeft;
        public ImageView senderPhotoImgRight;
        public ImageView sendStatusImg;
        public View cardCoverView;
        public TextView sendTimeText;
        public RelativeLayout cardParentLayout;

        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);
            cardLayout = (RelativeLayout) view
                    .findViewById(R.id.card_layout);
            senderNameText = (TextView) view
                    .findViewById(R.id.sender_name_text);
            senderPhotoImgLeft = (ImageView) view
                    .findViewById(R.id.iv_sender_photo_left);
            senderPhotoImgRight = (ImageView) view
                    .findViewById(R.id.iv_sender_photo_right);
            sendStatusImg = (ImageView) view.findViewById(R.id.send_status_img);
            cardCoverView = view.findViewById(R.id.card_cover_view);
            sendTimeText = (TextView) view
                    .findViewById(R.id.send_time_text);
            cardParentLayout = (RelativeLayout) view.findViewById(R.id.card_parent_layout);
        }

        /**
         * 实现OnClickListener接口重写的方法
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }

        }

        public void onMessageResendClick(UIMessage uiMessage){
            if (mListener != null) {
                mListener.onMessageResend(uiMessage);
            }

        }
    }


    /**
     * 显示正在发送的标志
     *
     * @param holder
     * @param msg
     */
    private void showRefreshingImg(final ViewHolder holder, final UIMessage uiMessage) {
        if (uiMessage.getSendStatus() == 0) {
            holder.sendStatusImg.setImageResource(R.drawable.pull_loading);
            RotateAnimation refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                    context, R.anim.pull_rotating);
            // 添加匀速转动动画
            LinearInterpolator lir = new LinearInterpolator();
            refreshingAnimation.setInterpolator(lir);
            holder.sendStatusImg.setVisibility(View.VISIBLE);
            holder.sendStatusImg.startAnimation(refreshingAnimation);
        } else if (uiMessage.getSendStatus() == 2) {
            holder.sendStatusImg.clearAnimation();
            holder.sendStatusImg.setVisibility(View.VISIBLE);
            holder.sendStatusImg.setImageResource(R.drawable.ic_chat_msg_send_fail);
        } else {
            holder.sendStatusImg.clearAnimation();
            boolean isMyMsg = uiMessage.getMessage().getFromUser().equals(MyApplication.getInstance().getUid());
            holder.sendStatusImg.setVisibility(isMyMsg ? View.INVISIBLE : View.GONE);
        }
        holder.sendStatusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    holder.onMessageResendClick(uiMessage);
            }
        });
    }

    /**
     * 显示卡片的内容
     *
     * @param holder
     * @param msg
     */
    private void showCardLayout(ViewHolder holder, final UIMessage uiMessage) {
        // TODO Auto-generated method stub
        Message message = uiMessage.getMessage();
        holder.cardLayout.removeAllViewsInLayout();
        holder.cardLayout.removeAllViews();
        boolean isMyMsg = message.getFromUser().equals(
                MyApplication.getInstance().getUid());
        holder.cardCoverView.setVisibility(View.GONE);
        View cardContentView;
        String type = message.getType();
        switch (type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:

                cardContentView = DisplayTxtPlainMsg.getView(context,
                        message);
                break;
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                cardContentView = DisplayTxtMarkdownMsg.getView(context,
                        message);
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                cardContentView = DisplayRegularFileMsg.getView(context,
                        message,uiMessage.getSendStatus());
                break;
            case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                cardContentView = DisplayAttachmentCardMsg.getView(context,
                        message);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_ACTIONS:
                cardContentView = DisplayExtendedActionsMsg.getInstance(context).getView(message);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                cardContentView = DisplayMediaImageMsg.getView(context,uiMessage);
                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                cardContentView = DisplayCommentTextPlainMsg.getView(context,message);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                cardContentView = DisplayExtendedLinksMsg.getView(context,message);
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                cardContentView = DisplayMediaVoiceMsg.getView(context,message);
                break;
            default:
                cardContentView = DisplayResUnknownMsg.getView(context, isMyMsg);
                break;
        }


        holder.cardLayout.addView(cardContentView);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.cardParentLayout.getLayoutParams();
        //此处实际执行params.removeRule();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        params.addRule(RelativeLayout.ALIGN_LEFT, 0);
        params.addRule(isMyMsg ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_LEFT);
        holder.cardParentLayout.setLayoutParams(params);
    }


    /**
     * 展示消息发送时间
     *
     * @param holder
     * @param msg
     * @param position
     */
    private void showMsgSendTime(ViewHolder holder, UIMessage UIMessage, int position) {
        // TODO Auto-generated method stub
        long lastMessageCreationDate = 0;
        if (position != 0) {
            lastMessageCreationDate = UIMessageList.get(position - 1).getCreationDate();
        }
        long duration = UIMessage.getCreationDate() - lastMessageCreationDate;
        if (duration >= 180000) {
            holder.sendTimeText.setVisibility(View.VISIBLE);
            String messageSendTime = TimeUtils.getChannelMsgDisplayTime(MyApplication.getInstance(), UIMessage.getCreationDate());
            holder.sendTimeText.setText(messageSendTime);
        } else {
            holder.sendTimeText.setVisibility(View.GONE);
        }
    }

    /**
     * 展示用户名称
     *
     * @param holder
     */
    private void showUserName(ViewHolder holder, UIMessage UIMessage) {
        // TODO Auto-generated method stub
        if (channelType.equals("GROUP") && !UIMessage.getMessage().getFromUser().equals(
                MyApplication.getInstance().getUid())){
            holder.senderNameText.setText(UIMessage.getSenderName());
        }else {
            holder.senderNameText.setVisibility(View.GONE);
        }
    }

    /**
     * 展示用户头像
     *
     * @param holder
     */
    private void showUserPhoto(ViewHolder holder, final UIMessage UImessage) {
        // TODO Auto-generated method stub
        final String fromUser = UImessage.getMessage().getFromUser();
        boolean isMyMsg = MyApplication.getInstance().getUid().equals(fromUser);
        holder.senderPhotoImgRight.setVisibility(isMyMsg?View.VISIBLE:View.INVISIBLE);
        holder.senderPhotoImgLeft.setVisibility(isMyMsg?View.INVISIBLE:View.VISIBLE);
        ImageView senderPhotoImg = isMyMsg?holder.senderPhotoImgRight:holder.senderPhotoImgLeft;
        ImageDisplayUtils.getInstance().displayImage(senderPhotoImg,
                UImessage.getSenderPhotoUrl(), R.drawable.icon_person_default);
        senderPhotoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("uid", fromUser);
                if (fromUser.startsWith("BOT") || channelType.endsWith("SERVICE")) {
                    bundle.putString("type", channelType);
                    IntentUtils.startActivity(context,
                            RobotInfoActivity.class, bundle);
                } else {
                    IntentUtils.startActivity(context,
                            UserInfoActivity.class, bundle);
                }
            }
        });
        senderPhotoImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (channelType.equals("GROUP")) {
                    chatInputMenu.addMentions(fromUser, UImessage.getSenderName(), false);
                }
                return true;
            }
        });
        }

    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
        void onMessageResend(UIMessage uiMessage);
    }
}