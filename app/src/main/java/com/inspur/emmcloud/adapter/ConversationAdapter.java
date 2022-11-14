package com.inspur.emmcloud.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.os.Build;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenmch on 2018/4/26.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<UIConversation> uiConversationList;
    private AdapterListener adapterListener;
    private Context context;

    private RecyclerView mRecyclerView;
    private View VIEW_HEADER;
    //Type
    private int TYPE_NORMAL = 1000;
    private int TYPE_HEADER = 1001;

    public ConversationAdapter(Context context, List<UIConversation> uiConversationList) {
        this.uiConversationList = uiConversationList;
        this.context = context;
        if (adapterListener != null) {
            adapterListener.onDataChange();
        }
    }

    public void setData(List<UIConversation> uiConversationList) {
        synchronized (this) {
            this.uiConversationList.clear();
            this.uiConversationList.addAll(uiConversationList);
        }
        if (adapterListener != null) {
            adapterListener.onDataChange();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new ViewHolder(VIEW_HEADER, adapterListener);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_view, parent, false);
            return  new ViewHolder(view, adapterListener);
        }
    }

    public boolean haveHeaderView() {
        return VIEW_HEADER != null;
    }

    private boolean isHeaderView(int position) {
        return haveHeaderView() && position == 0;
    }

    /**
     * 网络异常提示
     *
     * @param NetState 当前网络状态
     */
    public void setNetExceptionView(Boolean NetState) {
        if (false == NetState && !haveHeaderView()) {
            addHeaderView(LayoutInflater.from(context).inflate(R.layout.header_error, null));
        } else if (true == NetState && haveHeaderView()) {
            deleteHeaderView();
        }
    }

    /**
     * 添加异常headerView
     *
     * @param headerView 要添加的View
     */
    private void addHeaderView(View headerView) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerView.setLayoutParams(params);
        VIEW_HEADER = headerView;
        notifyItemInserted(0);
        mRecyclerView.getLayoutManager().scrollToPosition(0);
    }

    /**
     * 删除HeaderView
     */
    public void deleteHeaderView() {
        if (haveHeaderView()) {
            notifyItemRemoved(0);
            VIEW_HEADER = null;
        }
    }

    /**
     * 更新单个列表数据
     */
    public void notifyRealItemChanged(int position) {
        if (haveHeaderView()) {
            this.notifyItemChanged(position + 1);
        } else {
            this.notifyItemChanged(position);
        }
    }

    /**
     * 删除单个列表数据
     */
    public void notifyRealItemRemoved(int position) {
        if (haveHeaderView()) {
            this.notifyItemRemoved(position + 1);
        } else {
            this.notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        try {
            if (mRecyclerView == null && mRecyclerView != recyclerView) {
                mRecyclerView = recyclerView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!isHeaderView(position)) {
            if (haveHeaderView()) {
                position--;
            }
            UIConversation uiConversation = uiConversationList.get(position);
            holder.titleText.setText(uiConversation.getTitle());
            holder.timeText.setText(uiConversation.isServiceContainer() ? "" : TimeUtils.getDisplayTime(context, uiConversation.getLastUpdate()));
            holder.dndImg.setVisibility(uiConversation.getConversation().isDnd() ? VISIBLE : GONE);
            holder.mainLayout.setBackgroundResource(ResourceUtils.getResValueOfAttr(context, uiConversation.getConversation().isStick() ? R.attr.selector_list_top : R.attr.selector_list));
            if (uiConversation.getConversation().isStick()) {
                holder.mainLayout.setBackgroundResource(ResourceUtils.getResValueOfAttr(context, R.attr.selector_list_top));
            } else {
                holder.mainLayout.setBackgroundResource(ResourceUtils.getResValueOfAttr(context, R.attr.selector_list));
            }
            boolean isConversationTypeGroup = uiConversation.getConversation().getType().equals(Conversation.TYPE_GROUP);
            if (isConversationTypeGroup) {
                File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH + "/" + MyApplication.getInstance().getTanent() + uiConversation.getId() + "_100.png1");
                holder.photoImg.setTag("");
                if (file.exists()) {
                    holder.photoImg.setImageBitmap(ImageUtils.getBitmapByFile(file));
                } else {
                    holder.photoImg.setImageResource(R.drawable.icon_channel_group_default);
                }
            } else if (uiConversation.getConversation().getType().equals(Conversation.TYPE_TRANSFER)) { /**文件传输助手**/
                String conversationName = context.getString(R.string.chat_file_transfer);
                holder.titleText.setText(conversationName);
                ImageDisplayUtils.getInstance().displayImageByTag(holder.photoImg, uiConversation.getIcon(), R.drawable.ic_file_transfer);
            } else if (uiConversation.getConversation().getType().equals(Conversation.TYPE_SERVICE)) { /**服务号入口**/
                holder.titleText.setText(uiConversation.getTitle());
                ImageDisplayUtils.getInstance().displayImageByTag(holder.photoImg, uiConversation.getIcon(), R.drawable.ic_channel_service);
            } else if (uiConversation.getConversation().getType().equals(Conversation.TYPE_CAST)) { /**机器人、服务号频道**/
                Robot robot = RobotCacheUtils.getRobotById(context, uiConversation.getConversation().getName());
                if (robot != null) {
                    holder.titleText.setText(robot.getName());
                } else {
                    holder.titleText.setText(uiConversation.getConversation().getName());
                }
                ImageDisplayUtils.getInstance().displayImageByTag(holder.photoImg, uiConversation.getIcon(), R.drawable.ic_channel_service);
            } else {
                ImageDisplayUtils.getInstance().displayImageByTag(holder.photoImg, uiConversation.getIcon(), isConversationTypeGroup ? R.drawable.icon_channel_group_default : R.drawable.icon_person_default);
            }
            setConversationLastMessageSendStatus(holder, uiConversation);
            setConversationContent(holder, uiConversation);
            setConversationUnreadState(holder, uiConversation);
        }
    }

    /**
     * 设置频道中最后一条消息的消息状态
     *
     * @param holder
     * @param uiConversation
     */
    private void setConversationLastMessageSendStatus(ViewHolder holder, UIConversation uiConversation) {
        List<Message> messageList = uiConversation.getMessageList();
        if (messageList != null && messageList.size() > 0) {
            Message message = messageList.get(messageList.size() - 1);
            int status = message.getSendStatus();
            ;
//            if (message.getSendStatus() == Message.MESSAGE_SEND_ING) {
//                status = ChatFileUploadManagerUtils.getInstance().isMessageResourceUploading(
//                        message) ? Message.MESSAGE_SEND_ING : Message.MESSAGE_SEND_FAIL;
//            } else {
//                status = message.getSendStatus();
//            }
            switch (status) {
                case Message.MESSAGE_SEND_ING:
                    holder.sendStatusImg.setVisibility(VISIBLE);
                    holder.sendStatusImg.setImageResource(R.drawable.icon_message_sending);
                    break;
                case Message.MESSAGE_SEND_FAIL:
                    holder.sendStatusImg.setVisibility(VISIBLE);
                    holder.sendStatusImg.setImageResource(R.drawable.icon_message_send_fail);
                    break;
                default:
                    holder.sendStatusImg.setVisibility(GONE);
            }
        } else {
            holder.sendStatusImg.setVisibility(GONE);
        }
    }

    /**
     * 设置会话内容
     *
     * @param holder
     * @param uiConversation
     */
    private void setConversationContent(ViewHolder holder, UIConversation uiConversation) {
        if (uiConversation.isServiceContainer()) {
            holder.contentText.setText(R.string.service_conversation_desc);
            return;
        }
        String chatDrafts = uiConversation.getConversation().getDraft();
        if (!StringUtils.isBlank(chatDrafts)) {
            String content = "<font color='#FF0000'>" + context.getString(R.string.message_type_drafts) + "</font>" + chatDrafts;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.contentText.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, null, null));
            } else {
                holder.contentText.setText(Html.fromHtml(content));
            }
        } else {
            // 添加@用户/@所有人功能
            String nodeUserInfo = unreadMessageNodeMeOrAll(uiConversation);
            String content = uiConversation.getContent();
            if (!nodeUserInfo.isEmpty()) {
                content = "<font color='#FF0000'>" + context.getString(R.string.message_type_node_me) + "</font>" + nodeUserInfo;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.contentText.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, null, null));
                } else {
                    holder.contentText.setText(Html.fromHtml(content));
                }
            } else {
                holder.contentText.setText(content);
            }
        }
        TransHtmlToTextUtils.stripUnderlines(holder.contentText, R.color.msg_content_color);
    }


    /**
     * 设置会话已读未读状态
     *
     * @param holder
     * @param uiConversation
     */
    private void setConversationUnreadState(ViewHolder holder, UIConversation uiConversation) {
        holder.unreadLayout.setVisibility(uiConversation.getUnReadCount() > 0 ? VISIBLE : View.INVISIBLE);
        if (uiConversation.getUnReadCount() > 0) {
            if (uiConversation.getConversation().getType().equals(Conversation.TYPE_SERVICE)) {
                holder.unreadIcon.setVisibility(VISIBLE);
                holder.unreadText.setVisibility(GONE);
            } else {
                holder.unreadIcon.setVisibility(GONE);
                holder.unreadText.setVisibility(VISIBLE);
                holder.unreadText.setText(uiConversation.getUnReadCount() > 99 ? "99+" : "" + uiConversation.getUnReadCount());
            }
        }
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;

    }

    private String unreadMessageNodeMeOrAll(UIConversation uiConversation) {
        int count = uiConversation.getMessageList().size();
        List<Message> unReadMessageList = new ArrayList<>();
        if ((int) uiConversation.getUnReadCount() > count) {
            unReadMessageList = uiConversation.getMessageList();
        } else {
            unReadMessageList = uiConversation.getMessageList().subList(count - (int) uiConversation.getUnReadCount(), count);
        }
        if (unReadMessageList.isEmpty()) return "";
        Collections.reverse(unReadMessageList);
        for (Message message : unReadMessageList) {
            String content = message.getMsgContentTextPlain().getText();
            if (!StringUtils.isBlank(content)) {
                Map<String, String> mentionsMap = message.getMsgContentTextPlain().getMentionsMap();
                StringBuilder contentStringBuilder = new StringBuilder();
                contentStringBuilder.append(content);
                Pattern mentionPattern = Pattern.compile("@[a-z]*\\d+\\s");
                Matcher mentionMatcher = mentionPattern.matcher(contentStringBuilder);
                boolean isWhisperType = !message.getMsgContentTextPlain().getWhisperUsers().isEmpty();
                ArrayList<String> uidList = new ArrayList<>();
                while (mentionMatcher.find()) {
                    String patternString = mentionMatcher.group();
                    String key = patternString.substring(1, patternString.length() - 1).trim();
                    if (mentionsMap.containsKey(key)) {
                        String newString = "";
                        String uid = mentionsMap.get(key);
                        uidList.add(uid);
                        if ((uid.equals("EVERYBODY") || BaseApplication.getInstance().getUid().equals(uid))) {
                            if (isWhisperType) {
                                newString = ContactUserCacheUtils.getUserName(message.getFromUser()) + ": " + BaseApplication.getInstance().getString(R.string.send_a_whispers);
                            } else if (Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN.equals(message.getType())) {
                                newString = ContactUserCacheUtils.getUserName(message.getFromUser()) + ": " + BaseApplication.getInstance().getString(R.string.send_a_comment);
                            } else {
                                newString = ContactUserCacheUtils.getUserName(message.getFromUser()) + ": " + message.getShowContent();
                            }
                            contentStringBuilder.replace(0, contentStringBuilder.length(), newString);
                            return contentStringBuilder.toString();
                        }
                    }
                }
            }
        }
        return "";
    }

    @Override
    public int getItemCount() {
        int count = (uiConversationList == null ? 0 : uiConversationList.size());
        if (VIEW_HEADER != null) {
            count++;
        }
        return count;
    }

    public void notifyCommunicationDoubleClick() {
        if (mRecyclerView == null || !(mRecyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
            return;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        mShouldScroll = false;
//        int dndPosition = -1;
        //向下判断是否有未读消息，有跳出滑动到对应位置
        for (int i = firstVisibleItem + 1; i < uiConversationList.size(); i++) {
            UIConversation uiConversation = uiConversationList.get(i);
            if (uiConversation.getUnReadCount() > 0) {
//                if (!uiConversation.getConversation().isDnd()) {
                    smoothMoveToPosition(mRecyclerView, i);
//                    dndPosition = -1;
                    return;
//                }
//                if (dndPosition == -1) {
//                    dndPosition = i;
//                }
            }
        }
        //从0开始判断是否有未读消息，有跳出滑动到对应位置
        for (int i = 0; i <= firstVisibleItem; i++) {
            UIConversation uiConversation = uiConversationList.get(i);
            if (uiConversation.getUnReadCount() > 0) {
//                if (!uiConversation.getConversation().isDnd()) {
                smoothMoveToPosition(mRecyclerView, i);
//                    dndPosition = -1;
                return;
//                }
//                if (dndPosition == -1) {
//                    dndPosition = i;
//                }
            }
        }

        //跳转到免打扰的消息
//        if (dndPosition != -1) {
//            smoothMoveToPosition(mRecyclerView, dndPosition);
//            dndPosition = -1;
//            return;
//        }

        smoothMoveToPosition(mRecyclerView, 0);
    }

    private void scrollToPosition(final LinearLayoutManager linearLayoutManager, final int i) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                linearLayoutManager.scrollToPositionWithOffset(i, 0);
                linearLayoutManager.setStackFromEnd(true);
//                mRecyclerView.smoothScrollToPosition(i);
            }
        });
    }

    public boolean ismShouldScroll() {
        return mShouldScroll;
    }

    public void setmShouldScroll(boolean mShouldScroll) {
        this.mShouldScroll = mShouldScroll;
    }

    public int getmToPosition() {
        return mToPosition;
    }

    /**
     * 目标项是否在最后一个可见项之后
     */
    private boolean mShouldScroll;

    /**
     * 记录目标项位置
     */
    private int mToPosition;

    /**
     * 滑动到指定位置
     *
     * @param mRecyclerView
     * @param position
     */
    public void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));

        if (position < firstItem) {
            // 如果跳转位置在第一个可见位置之前，就smoothScrollToPosition可以直接跳转
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 跳转位置在第一个可见项之后，最后一个可见项之前
            // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
    }

    /**
     * 创建一个回调接口
     */
    public interface AdapterListener {
        void onItemClick(View view, UIConversation uiConversation);

        boolean onItemLongClick(View view, UIConversation uiConversation);

        void onDataChange();

        void onNetExceptionWightClick();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private RelativeLayout mainLayout;
        private CircleTextImageView photoImg;
        private ImageView sendStatusImg;
        private TextView contentText;
        private TextView titleText;
        private TextView timeText;
        private RelativeLayout unreadLayout;
        private TextView unreadText;
        private ImageView dndImg;
        private AdapterListener adapterListener;
        private ImageView unreadIcon;

        public ViewHolder(View convertView, AdapterListener adapterListener) {
            super(convertView);
            this.adapterListener = adapterListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mainLayout = (RelativeLayout) convertView
                    .findViewById(R.id.main_layout);
            photoImg = (CircleTextImageView) convertView
                    .findViewById(R.id.msg_img);
            titleText = (TextView) convertView
                    .findViewById(R.id.tv_name);
            contentText = (TextView) convertView
                    .findViewById(R.id.tv_content);
            timeText = (TextView) convertView
                    .findViewById(R.id.time_text);
            unreadLayout = (RelativeLayout) convertView
                    .findViewById(R.id.msg_new_layout);
            unreadText = (TextView) convertView
                    .findViewById(R.id.msg_new_text);
            dndImg = (ImageView) convertView
                    .findViewById(R.id.msg_dnd_img);
            sendStatusImg = (ImageView) convertView
                    .findViewById(R.id.img_sending_status);
            unreadIcon = (ImageView) convertView
                    .findViewById(R.id.msg_new_icon);
        }


        @Override
        public void onClick(View v) {
            //notify未完成时，调用getAdapterPosition就会返回NO_POSITION(-1)
            if (getAdapterPosition() < 0) {
                return;
            }
            if (adapterListener != null) {
                if (haveHeaderView()) {
                    if (0 == getAdapterPosition()) {
                        adapterListener.onNetExceptionWightClick();   //点击进入新的Activity
                    } else {
                        adapterListener.onItemClick(v, uiConversationList.get(getAdapterPosition() - 1));
                    }
                } else {
                    adapterListener.onItemClick(v, uiConversationList.get(getAdapterPosition()));
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            //notify未完成时，调用getAdapterPosition就会返回NO_POSITION(-1)
            if (getAdapterPosition() < 0) {
                return false;
            }
            if (adapterListener != null) {
                if (haveHeaderView()) {
                    if (0 == getAdapterPosition()) {
                        LogUtils.LbcDebug("onLongClick");
                        return true;
                    } else {
                        //网络异常状态
                        return adapterListener.onItemLongClick(v, uiConversationList.get(getAdapterPosition() - 1));
                    }
                } else {
                    return adapterListener.onItemLongClick(v, uiConversationList.get(getAdapterPosition()));
                }

            }
            return false;
        }
    }

}

