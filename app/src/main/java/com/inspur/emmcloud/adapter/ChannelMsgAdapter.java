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
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.ui.chat.ChannelMsgDetailActivity;
import com.inspur.emmcloud.ui.chat.DisplayResFileMsg;
import com.inspur.emmcloud.ui.chat.DisplayResImageMsg;
import com.inspur.emmcloud.ui.chat.DisplayResLinkMsg;
import com.inspur.emmcloud.ui.chat.DisplayResUnknownMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtCommentMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtRichMsg;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by chenmch on 2017/11/10.
 */

public class ChannelMsgAdapter extends RecyclerView.Adapter<ChannelMsgAdapter.ViewHolder> {
    private Activity context;
    private List<Msg> msgList = new ArrayList<>();
    private MyItemClickListener mItemClickListener;
    private ChatAPIService apiService;
    private String channelType;
    private ECMChatInputMenu chatInputMenu;

    public ChannelMsgAdapter(Activity context, ChatAPIService apiService, String channelType, ECMChatInputMenu chatInputMenu) {
        this.context = context;
        this.apiService = apiService;
        this.channelType = channelType;
        this.chatInputMenu = chatInputMenu;
    }

    public void setMsgList(List<Msg> msgList) {
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
        final Msg msg = msgList.get(position);
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
    private void showRefreshingImg(ViewHolder holder, Msg msg) {
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
    private void showCardLayout(ViewHolder holder, final Msg msg) {
        // TODO Auto-generated method stub
        holder.cardLayout.removeAllViewsInLayout();
        holder.cardLayout.removeAllViews();
        boolean isMyMsg = msg.getUid().equals(
                ((MyApplication) context.getApplicationContext()).getUid());
        holder.cardCoverView.setVisibility(View.VISIBLE);
        View childView = null;
        LayoutInflater vi = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        String type = msg.getType();
        if (type.equals("txt_comment") || type.equals("comment")) {
            holder.cardCoverView.setVisibility(View.GONE);
            childView = vi.inflate(
                    R.layout.chat_msg_card_child_text_comment_view, null);
            DisplayTxtCommentMsg.displayCommentMsg(context,
                    childView, msg, apiService);
        } else if (type.equals("res_image") || type.equals("image")) {
            childView = vi.inflate(
                    R.layout.chat_msg_card_child_res_img_view, null);
            DisplayResImageMsg.displayResImgMsg(context,
                    childView, msg);
        } else if (type.equals("res_link")) {
            holder.newsCommentText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Bundle bundle = new Bundle();
                    bundle.putString("mid", msg.getMid());
                    bundle.putString("cid", msg.getCid());
                    IntentUtils.startActivity(context,
                            ChannelMsgDetailActivity.class, bundle);
                }
            });
            childView = vi.inflate(
                    R.layout.chat_msg_card_child_res_link_view, null);
            DisplayResLinkMsg.displayResLinkMsg(context,
                    childView, msg);
        } else if (type.equals("res_file")) {
            childView = vi.inflate(
                    R.layout.chat_msg_card_child_res_file_view, null);
            DisplayResFileMsg.displayResFileMsg(context,
                    childView, msg);
        } else if (type.equals("txt_rich")) {
            holder.cardCoverView.setVisibility(View.GONE);
            childView = vi.inflate(
                    R.layout.chat_msg_card_child_text_rich_view, null);
            DisplayTxtRichMsg.displayRichTextMsg(context,
                    childView, msg);
        } else {
            childView = vi.inflate(
                    R.layout.chat_msg_card_child_res_unknown_view, null);
            DisplayResUnknownMsg.displayResUnknownMsg(context,
                    childView, msg);
        }
        holder.cardLayout.addView(childView);
        holder.cardLayout.setBackgroundColor(context.getResources().getColor(
                isMyMsg ? R.color.bg_my_card : R.color.white));
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
    private void showMsgSendTime(ViewHolder holder, Msg msg, int position) {
        // TODO Auto-generated method stub
        long msgTimeLong = TimeUtils.UTCString2Long(msg.getTime());
        long lastMsgTimelong = 0;
        if (position != 0) {
            lastMsgTimelong = TimeUtils.UTCString2Long(msgList.get(
                    position - 1).getTime());
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
        boolean isMyMsg = msg.getUid().equals(
                ((MyApplication) context.getApplicationContext()).getUid());
        if (channelType.equals("GROUP") && !isMyMsg) {
            holder.senderNameText.setVisibility(View.VISIBLE);
            holder.senderNameText.setText(msg.getTitle());
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
        if (msg.getUid().equals(
                ((MyApplication) context.getApplicationContext()).getUid())) {
            holder.senderPhotoImg.setVisibility(View.INVISIBLE);
        } else {
            holder.senderPhotoImg.setVisibility(View.VISIBLE);
            String iconUrl ="";
            if (msg.getUid().startsWith("BOT") || channelType.equals("SERVICE")) {
                iconUrl = APIUri.getRobotIconUrl(RobotCacheUtils
                        .getRobotById(context, msg.getUid())
                        .getAvatar());
            }else {
                iconUrl = APIUri.getChannelImgUrl(context, msg.getUid());
            }
            ImageDisplayUtils.getInstance().displayImage(holder.senderPhotoImg,
                    iconUrl, R.drawable.icon_person_default);
            holder.senderPhotoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    String uid = msg.getUid();
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
            holder.senderPhotoImg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (channelType.equals("GROUP")) {
                        chatInputMenu.addMentions(msg.getUid(), msg.getTitle(), false);
                    }
                    return true;
                }
            });

        }
    }

    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }
}