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
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.ui.chat.DisplayAttachmentCardMsg;
import com.inspur.emmcloud.ui.chat.DisplayAttachmentFileMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedActionsMsg;
import com.inspur.emmcloud.ui.chat.DisplayResUnknownMsgRobot;
import com.inspur.emmcloud.ui.chat.DisplayTxtPlainMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtRichMsgRobot;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenuRobot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/10.
 */

public class ChannelMsgAdapterRobot extends RecyclerView.Adapter<ChannelMsgAdapterRobot.ViewHolder> {
    private Activity context;
    private List<MsgRobot> msgList = new ArrayList<>();
    private MyItemClickListener mItemClickListener;
    private ChatAPIService apiService;
    private String channelType;
    private ECMChatInputMenuRobot chatInputMenu;

    public ChannelMsgAdapterRobot(Activity context, ChatAPIService apiService, String channelType, ECMChatInputMenuRobot chatInputMenu) {
        this.context = context;
        this.apiService = apiService;
        this.channelType = channelType;
        this.chatInputMenu = chatInputMenu;
    }

    public void setMsgList(List<MsgRobot> msgList) {
        this.msgList.clear();
        this.msgList.addAll(msgList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_msg_card_parent_view, viewGroup, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MsgRobot msg = msgList.get(position);
        showCardLayout(holder, msg);
        showUserName(holder, msg);
        showMsgSendTime(holder, msg, position);
        showUserPhoto(holder, msg);
        showRefreshingImg(holder, msg);
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


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener mListener;
        public RelativeLayout cardLayout;
        public TextView senderNameText;
        public ImageView senderPhotoImg;
        public ImageView refreshingImg;
        public View cardCoverView;
        public TextView sendTimeText;
        public TextView newsCommentText;

        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);
            cardLayout = (RelativeLayout) view
                    .findViewById(R.id.card_layout);
            senderNameText = (TextView) view
                    .findViewById(R.id.sender_name_text);
            senderPhotoImg = (ImageView) view
                    .findViewById(R.id.sender_photo_img);
            refreshingImg = (ImageView) view.findViewById(R.id.refreshing_img);
            cardCoverView = view.findViewById(R.id.card_cover_view);
            sendTimeText = (TextView) view
                    .findViewById(R.id.send_time_text);
            newsCommentText = (TextView) view
                    .findViewById(R.id.news_comment_text);
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
    }


    /**
     * 显示正在发送的标志
     *
     * @param holder
     * @param msg
     */
    private void showRefreshingImg(ViewHolder holder, MsgRobot msg) {
        if (msg.getSendStatus() == 0) {
            holder.refreshingImg.setImageResource(R.drawable.pull_loading);
            RotateAnimation refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                    context, R.anim.pull_rotating);
            // 添加匀速转动动画
            LinearInterpolator lir = new LinearInterpolator();
            refreshingAnimation.setInterpolator(lir);
            holder.refreshingImg.setVisibility(View.VISIBLE);
            holder.refreshingImg.startAnimation(refreshingAnimation);
        } else if (msg.getSendStatus() == 2) {
            holder.refreshingImg.clearAnimation();
            holder.refreshingImg.setVisibility(View.VISIBLE);
            holder.refreshingImg.setImageResource(R.drawable.ic_chat_msg_send_fail);
        } else {
            holder.refreshingImg.clearAnimation();
            holder.refreshingImg.setVisibility(View.GONE);
        }

    }

    /**
     * 显示卡片的内容
     *
     * @param holder
     * @param msg
     */
    private void showCardLayout(ViewHolder holder, final MsgRobot msg) {
        // TODO Auto-generated method stub
        holder.cardLayout.removeAllViewsInLayout();
        holder.cardLayout.removeAllViews();
        boolean isMyMsg = msg.getFromUser().equals(
                MyApplication.getInstance().getUid());
        holder.cardCoverView.setVisibility(View.VISIBLE);
        View cardContentView = null;
        switch (msg.getType()) {
            case "text/plain":
                holder.cardCoverView.setVisibility(View.GONE);
                cardContentView = DisplayTxtPlainMsg.getView(context,
                        msg);
                break;
            case "text/markdown":
                holder.cardCoverView.setVisibility(View.GONE);
                cardContentView = DisplayTxtRichMsgRobot.getView(context,
                        msg);
                break;
            case "attachment/file":
                cardContentView = DisplayAttachmentFileMsg.getView(context,
                        msg, false);
                break;
//            case "comment/text-plain":
//                holder.cardCoverView.setVisibility(View.GONE);
//                cardContentView = DisplayTxtCommentMsg.getView(context,msg, apiService);
//                break;
//            case "media/image":
//                cardContentView = DisplayMediaImageMsg.getView(context,
//                        msg);
//                break;
            case "attachment/card":
                cardContentView = DisplayAttachmentCardMsg.getView(context,
                        msg);
                break;
//            case "extended/links":
//                holder.newsCommentText.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // TODO Auto-generated method stub
//                        Bundle bundle = new Bundle();
//                        bundle.putString("mid", msg.getFromUser());
//                        bundle.putString("cid", msg.getChannel());
//                        IntentUtils.startActivity(context,
//                                ChannelMsgDetailActivity.class, bundle);
//                    }
//                });
//                cardContentView =  DisplayExtendedLinksMsg.getView(context,
//                        msg);
//                break;
            case "extended/actions":
                cardContentView = DisplayExtendedActionsMsg.getView(context, msg);
                break;
            default:
                cardContentView = DisplayResUnknownMsgRobot.getView(context,
                        msg);
                break;
        }
        holder.cardLayout.addView(cardContentView);
        holder.cardCoverView.setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_img_cover_arrow_right : R.drawable.ic_chat_msg_img_cover_arrow_left);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.cardLayout.getLayoutParams();
        //此处实际执行params.removeRule();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        params.addRule(RelativeLayout.ALIGN_LEFT, 0);
        params.addRule(isMyMsg ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_LEFT);
        holder.cardLayout.setLayoutParams(params);
    }


    /**
     * 展示消息发送时间
     *
     * @param holder
     * @param msg
     * @param position
     */
    private void showMsgSendTime(ViewHolder holder, MsgRobot msg, int position) {
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
    private void showUserName(ViewHolder holder, MsgRobot msg) {
        // TODO Auto-generated method stub
        boolean isMyMsg = msg.getFromUser().equals(
                MyApplication.getInstance().getUid());
        if (channelType.equals("GROUP") && !isMyMsg) {
            holder.senderNameText.setVisibility(View.VISIBLE);
            holder.senderNameText.setText(ContactCacheUtils.getUserName(MyApplication.getInstance(), msg.getFromUser()));
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
    private void showUserPhoto(ViewHolder holder, final MsgRobot msg) {
        // TODO Auto-generated method stub
        if (msg.getFromUser().equals(
                MyApplication.getInstance().getUid())) {
            holder.senderPhotoImg.setVisibility(View.INVISIBLE);
        } else {
            holder.senderPhotoImg.setVisibility(View.VISIBLE);
            String iconUrl = "";
            if (msg.getFromUser().startsWith("BOT") || channelType.equals("SERVICE")) {
                iconUrl = APIUri.getRobotIconUrl(RobotCacheUtils
                        .getRobotById(context, msg.getFromUser())
                        .getAvatar());
            } else {
                iconUrl = APIUri.getChannelImgUrl(context, msg.getFromUser());
            }
            ImageDisplayUtils.getInstance().displayImage(holder.senderPhotoImg,
                    iconUrl, R.drawable.icon_person_default);
            holder.senderPhotoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    String uid = msg.getFromUser();
                    bundle.putString("uid", uid);
                    if (uid.startsWith("BOT") || channelType.endsWith("SERVICE")) {
                        bundle.putString("type", channelType);
                        IntentUtils.startActivity(context,
                                RobotInfoActivity.class, bundle);
                    } else {
                        IntentUtils.startActivity(context,
                                UserInfoActivity.class, bundle);
                    }
                }
            });
//            holder.senderPhotoImg.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    if (channelType.equals("GROUP")) {
//                        chatInputMenu.addMentions(msg.getFromUser(), false);
//                    }
//                    return true;
//                }
//            });

        }
    }

    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }
}