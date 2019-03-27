package com.inspur.emmcloud.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.inspur.emmcloud.widget.bubble.BubbleLayout;
import com.qmuiteam.qmui.widget.QMUILoadingView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/10.
 */

public class ChannelMessageAdapter extends RecyclerView.Adapter<ChannelMessageAdapter.ViewHolder> {
    private Activity context;
    private List<UIMessage> UIMessageList = new ArrayList<>();
    private MyItemClickListener mItemClickListener;
    private CardItemLongClickListener cardItemLongClickListener;
    private CardItemClickListener cardItemClickListener;
    private String channelType;
    private ECMChatInputMenu chatInputMenu;

    public ChannelMessageAdapter(Activity context, String channelType, ECMChatInputMenu chatInputMenu) {
        this.context = context;
        this.channelType = channelType;
        this.chatInputMenu = chatInputMenu;
        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mItemClickListener != null) {
                    mItemClickListener.onAdapterDataSizeChange();
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (mItemClickListener != null) {
                    mItemClickListener.onAdapterDataSizeChange();
                }
            }
        });
    }

    public void setMessageList(List<UIMessage> UImessageList) {
        this.UIMessageList.clear();
        this.UIMessageList.addAll(UImessageList);
    }

    public void setChannelData(String channelType, ECMChatInputMenu chatInputMenu) {
        this.channelType = channelType;
        this.chatInputMenu = chatInputMenu;
    }

    public UIMessage getItemData(int position) {
        return this.UIMessageList.get(position);
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

    /**
     * Activity 实现卡片长按监听接口函数并通过该方法添加到卡片上*/
    public void setCardItemLongClickListener(CardItemLongClickListener CardItemLongClickListener){
        this.cardItemLongClickListener=CardItemLongClickListener;
    }

    /**
     * Activity 实现卡片点击监听听接口函数并通过该方法添加到卡片上*/
    public void setCardItemClickListener(CardItemClickListener cardItemClickListener){
        this.cardItemClickListener=cardItemClickListener;
    }

    @Override
    public int getItemCount() {
        return UIMessageList.size();
    }

    /**
     * 显示正在发送的标志
     *
     * @param holder
     */
    public void showRefreshingImg(final ViewHolder holder, final UIMessage uiMessage) {
        if (uiMessage.getSendStatus() == 0) {
            holder.sendStatusLayout.setVisibility(View.VISIBLE);
            holder.sendFailImg.setVisibility(View.GONE);
            holder.sendingLoadingView.setVisibility(View.VISIBLE);
        } else if (uiMessage.getSendStatus() == 2) {
            holder.sendStatusLayout.setVisibility(View.VISIBLE);
            holder.sendFailImg.setVisibility(View.VISIBLE);
            holder.sendingLoadingView.setVisibility(View.GONE);
        } else {
            boolean isMyMsg = uiMessage.getMessage().getFromUser().equals(MyApplication.getInstance().getUid());
            holder.sendStatusLayout.setVisibility(isMyMsg ? View.INVISIBLE : View.GONE);
        }
        holder.sendStatusLayout.setOnClickListener(new View.OnClickListener() {
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
     */
    private void showCardLayout(ViewHolder holder, final UIMessage uiMessage) {
        // TODO Auto-generated method stub
        Message message = uiMessage.getMessage();
        boolean isMyMsg = message.getFromUser().equals(
                MyApplication.getInstance().getUid());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.cardParentLayout.getLayoutParams();
        //此处实际执行params.removeRule();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        params.addRule(RelativeLayout.ALIGN_LEFT, 0);
        params.addRule(isMyMsg ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_LEFT);
        holder.cardParentLayout.setLayoutParams(params);
        holder.cardLayout.removeAllViewsInLayout();
        holder.cardLayout.removeAllViews();
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
                        message, uiMessage.getSendStatus(), false);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                cardContentView = DisplayAttachmentCardMsg.getView(context,
                        message);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_ACTIONS:
                cardContentView = DisplayExtendedActionsMsg.getInstance(context).getView(message);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                cardContentView = DisplayMediaImageMsg.getView(context, uiMessage);
                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                cardContentView = DisplayCommentTextPlainMsg.getView(context, message);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                cardContentView = DisplayExtendedLinksMsg.getView(context, message);
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                cardContentView = DisplayMediaVoiceMsg.getView(context, uiMessage, mItemClickListener);
                break;
            default:
                cardContentView = DisplayResUnknownMsg.getView(context, isMyMsg);
                break;
        }


        holder.cardLayout.addView(cardContentView);
        cardContentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return  cardItemLongClickListener.onItemLongClick(view,uiMessage);
        }});
        cardContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardItemClickListener.onCardItemClick(view,uiMessage);
            }
        });

    }

    /**
     * 展示消息发送时间
     *
     * @param holder
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
            String messageSendTime = TimeUtils.getChannelMsgDisplayTime(context, UIMessage.getCreationDate());
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
                MyApplication.getInstance().getUid())) {
            holder.senderNameText.setVisibility(View.VISIBLE);
            holder.senderNameText.setText(UIMessage.getSenderName());
        } else {
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
        holder.senderPhotoImgRight.setVisibility(isMyMsg ? View.VISIBLE : View.INVISIBLE);
        holder.senderPhotoImgLeft.setVisibility(isMyMsg ? View.GONE : View.VISIBLE);
        ImageView senderPhotoImg = isMyMsg ? holder.senderPhotoImgRight : holder.senderPhotoImgLeft;
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

        void onMessageResend(UIMessage uiMessage, View view);

        void onMediaVoiceReRecognize(UIMessage uiMessage, BubbleLayout bubbleLayout, QMUILoadingView downloadLoadingView);

        void onAdapterDataSizeChange();
    }

    /**
     *沟通 卡片长按监听接口*/
    public interface CardItemLongClickListener{
        boolean onItemLongClick(View view,UIMessage uiMessage);
    }

    /**
     *沟通 卡片点击事件监听接口*/
    public interface CardItemClickListener{
        void onCardItemClick(View view,UIMessage uiMessage);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener   {
        public RelativeLayout cardLayout;
        public TextView senderNameText;
        public ImageView senderPhotoImgLeft;
        public ImageView senderPhotoImgRight;
        public RelativeLayout sendStatusLayout;
        public ImageView sendFailImg;
        public QMUILoadingView sendingLoadingView;
        public TextView sendTimeText;
        public RelativeLayout cardParentLayout;
        private MyItemClickListener mListener;

        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);
            cardLayout = (RelativeLayout) view
                    .findViewById(R.id.bll_card);
            senderNameText = (TextView) view
                    .findViewById(R.id.sender_name_text);
            senderPhotoImgLeft = (ImageView) view
                    .findViewById(R.id.iv_sender_photo_left);
            senderPhotoImgRight = (ImageView) view
                    .findViewById(R.id.iv_sender_photo_right);
            sendStatusLayout = (RelativeLayout) view.findViewById(R.id.rl_send_status);
            sendFailImg = (ImageView) view.findViewById(R.id.iv_send_fail);
            sendingLoadingView = (QMUILoadingView) view.findViewById(R.id.qlv_sending);
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

        public void onMessageResendClick(UIMessage uiMessage) {
            if (mListener != null) {
                mListener.onMessageResend(uiMessage, sendFailImg);
            }

        }
    }
}