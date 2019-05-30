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
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.ui.chat.DisplayAttachmentCardMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedActionsMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedDecideMsg;
import com.inspur.emmcloud.ui.chat.DisplayRegularFileMsg;
import com.inspur.emmcloud.ui.chat.DisplayResFileMsg;
import com.inspur.emmcloud.ui.chat.DisplayResImageMsg;
import com.inspur.emmcloud.ui.chat.DisplayResLinkMsg;
import com.inspur.emmcloud.ui.chat.DisplayResUnknownMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtCommentMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtMarkdownMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtPlainMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtRichMsg;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.CustomLoadingView;
import com.inspur.emmcloud.widget.ECMChatInputMenuV0;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/10.
 */

public class ChannelMsgAdapter extends RecyclerView.Adapter<ChannelMsgAdapter.ViewHolder> {
    private Activity context;
    private List<Msg> msgList = new ArrayList<>();
    private MyItemClickListener mItemClickListener;
    private ChatAPIService apiService;
    private String channelType;
    private ECMChatInputMenuV0 chatInputMenu;

    public ChannelMsgAdapter(Activity context, ChatAPIService apiService, String channelType, ECMChatInputMenuV0 chatInputMenu) {
        this.context = context;
        this.apiService = apiService;
        this.channelType = channelType;
        this.chatInputMenu = chatInputMenu;
    }

    public void setMsgList(List<Msg> msgList) {
        this.msgList.clear();
        this.msgList.addAll(msgList);
    }

    public Msg getItemData(int position) {
        return this.msgList.get(position);
    }

    public void setChannelData(String channelType, ECMChatInputMenuV0 chatInputMenu) {
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
        final Msg msg = msgList.get(position);
        showUserName(holder, msg);
        showMsgSendTime(holder, msg, position);
        showUserPhoto(holder, msg);
        showRefreshingImg(holder, msg);
        showCardLayout(holder, msg);
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
        return msgList.size();
    }

    /**
     * 显示正在发送的标志
     *
     * @param holder
     * @param msg
     */
    private void showRefreshingImg(final ViewHolder holder, final Msg msg) {
        if (msg.getSendStatus() == 0) {
            holder.sendStatusLayout.setVisibility(View.VISIBLE);
            holder.sendFailImg.setVisibility(View.GONE);
            holder.sendingLoadingView.setVisibility(View.VISIBLE);
        } else if (msg.getSendStatus() == 2) {
            holder.sendStatusLayout.setVisibility(View.VISIBLE);
            holder.sendFailImg.setVisibility(View.VISIBLE);
            holder.sendingLoadingView.setVisibility(View.GONE);
        } else {
            boolean isMyMsg;
            if (Message.isMessage(msg)) {
                Message message = new Message(msg);
                isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
            } else {
                isMyMsg = msg.getUid().equals(MyApplication.getInstance().getUid());
            }
            holder.sendStatusLayout.setVisibility(isMyMsg ? View.INVISIBLE : View.GONE);
        }
        holder.sendStatusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.onMessageResendClick(msg);
            }
        });
    }

    /**
     * 显示卡片的内容
     *
     * @param holder
     * @param msg
     */
    private void showCardLayout(ViewHolder holder, final Msg msg) {
        // TODO Auto-generated method stub
        holder.cardLayout.removeAllViewsInLayout();
        holder.cardLayout.removeAllViews();
        boolean isMyMsg = msg.getUid().equals(
                MyApplication.getInstance().getUid());
        View cardContentView;
        Message message = null;
        String type = msg.getType();
        if (Message.isMessage(msg)) {
            message = new Message(msg);
            type = message.getType();
            isMyMsg = message.getFromUser().equals(
                    MyApplication.getInstance().getUid());
        }
        switch (type) {
            case "txt_comment":
            case "comment":
                cardContentView = DisplayTxtCommentMsg.displayCommentMsg(context, msg, apiService);
                break;
            case "res_image":
            case "image":
                cardContentView = DisplayResImageMsg.displayResImgMsg(context,
                        msg);
                break;
            case "res_link":
                cardContentView = DisplayResLinkMsg.displayResLinkMsg(context, msg);
                break;
            case "res_file":
                cardContentView = DisplayResFileMsg.displayResFileMsg(context, msg, false);
                break;
            case "txt_rich":
                cardContentView = DisplayTxtRichMsg.displayRichTextMsg(context, msg);
                break;

            case "text/plain":
                cardContentView = DisplayTxtPlainMsg.getView(context,
                        message);
                break;
            case "text/markdown":
                cardContentView = DisplayTxtMarkdownMsg.getView(context,
                        message);
                break;
            case "attachment/file":
                cardContentView = DisplayRegularFileMsg.getView(context,
                        message, 1, false);
                break;
            case "attachment/card":
                cardContentView = DisplayAttachmentCardMsg.getView(context,
                        message);
                break;
            case "extended/actions":
                cardContentView = DisplayExtendedActionsMsg.getInstance(context).getView(message);
                break;
//            case "extended/selects":
//                LogUtils.YfcDebug("v0决策卡片");
//                cardContentView = DisplayExtendedDecideMsg.getView(message,context);
//                break;
            case "experimental/selects":
                LogUtils.YfcDebug("v0决策卡片");
                cardContentView = DisplayExtendedDecideMsg.getView(message,context);
                break;
            default:
                cardContentView = DisplayResUnknownMsg.getView(context,
                        isMyMsg);
                break;
        }


        holder.cardLayout.addView(cardContentView);
//        holder.cardLayout.setBackgroundColor(context.getResources().getColor(
//                isMyMsg ? R.color.bg_my_card : R.color.white));
//        holder.cardCoverView.setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_img_cover_arrow_right : R.drawable.ic_chat_msg_img_cover_arrow_left);
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
    private void showMsgSendTime(ViewHolder holder, Msg msg, int position) {
        // TODO Auto-generated method stub
        long msgTimeLong = msg.getTime();
        long lastMsgTimelong = 0;
        if (position != 0) {
            lastMsgTimelong = msgList.get(
                    position - 1).getTime();
        }
        long duration = msgTimeLong - lastMsgTimelong;
        if (duration >= 180000) {
            holder.sendTimeText.setVisibility(View.VISIBLE);
            String msgSendTime = TimeUtils.getChannelMsgDisplayTime(
                    context, msg.getTime());
            holder.sendTimeText.setText(msgSendTime);
        } else {
            holder.sendTimeText.setVisibility(View.GONE);
        }
    }

    /**
     * 展示用户名称
     *
     * @param holder
     * @param msg
     */
    private void showUserName(ViewHolder holder, Msg msg) {
        // TODO Auto-generated method stub
        if (channelType.equals("GROUP") && !msg.getUid().equals(MyApplication.getInstance().getUid())) {
            String userName = ContactUserCacheUtils.getUserName(msg.getUid());
            holder.senderNameText.setVisibility(View.VISIBLE);
            holder.senderNameText.setText(userName);
        } else {
            holder.senderNameText.setVisibility(View.GONE);
        }

    }

    /**
     * 展示用户头像
     *
     * @param holder
     * @param msg
     */
    private void showUserPhoto(ViewHolder holder, final Msg msg) {
        // TODO Auto-generated method stub
        final String fromUserUid;
        if (Message.isMessage(msg)) {
            Message message = new Message(msg);
            fromUserUid = message.getFromUser();
        } else {
            fromUserUid = msg.getUid();
        }
        boolean isMyMsg = MyApplication.getInstance().getUid().equals(fromUserUid);
        holder.senderPhotoImgRight.setVisibility(isMyMsg ? View.VISIBLE : View.INVISIBLE);
        holder.senderPhotoImgLeft.setVisibility(isMyMsg ? View.GONE : View.VISIBLE);
        String iconUrl = APIUri.getUserIconUrl(context, fromUserUid);
        ImageView senderPhotoImg = isMyMsg ? holder.senderPhotoImgRight : holder.senderPhotoImgLeft;

        ImageDisplayUtils.getInstance().displayImage(senderPhotoImg,
                iconUrl, R.drawable.icon_person_default);
        senderPhotoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("uid", fromUserUid);
                if (fromUserUid.startsWith("BOT") || channelType.endsWith("SERVICE")) {
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
                if (channelType.equals("GROUP") && !MyApplication.getInstance().getUid().equals(fromUserUid)) {
                    chatInputMenu.addMentions(msg.getUid(), msg.getTitle(), false);
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

        void onMessageResend(Msg msg);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public RelativeLayout cardLayout;
        public TextView senderNameText;
        public ImageView senderPhotoImgLeft;
        public ImageView senderPhotoImgRight;
        public RelativeLayout sendStatusLayout;
        public TextView sendTimeText;
        public RelativeLayout cardParentLayout;
        private MyItemClickListener mListener;
        private ImageView sendFailImg;
        private CustomLoadingView sendingLoadingView;

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
            sendingLoadingView = (CustomLoadingView) view.findViewById(R.id.qlv_sending);
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

        public void onMessageResendClick(Msg msg) {
            if (mListener != null) {
                mListener.onMessageResend(msg);
            }

        }
    }
}