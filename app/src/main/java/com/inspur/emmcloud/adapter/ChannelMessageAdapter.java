package com.inspur.emmcloud.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ChannelMessageStates;
import com.inspur.emmcloud.basemodule.media.selector.dialog.RemindDialog;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.DisplayAttachmentCardMsg;
import com.inspur.emmcloud.ui.chat.DisplayCommentNewMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedActionsMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedDecideMsg;
import com.inspur.emmcloud.ui.chat.DisplayExtendedLinksMsg;
import com.inspur.emmcloud.ui.chat.DisplayMediaImageMsg;
import com.inspur.emmcloud.ui.chat.DisplayMediaVideoMsg;
import com.inspur.emmcloud.ui.chat.DisplayMediaVoiceMsg;
import com.inspur.emmcloud.ui.chat.DisplayMultiMsg;
import com.inspur.emmcloud.ui.chat.DisplayRecallMsg;
import com.inspur.emmcloud.ui.chat.DisplayRegularFileMsg;
import com.inspur.emmcloud.ui.chat.DisplayResUnknownMsg;
import com.inspur.emmcloud.ui.chat.DisplayServiceCommentTextPlainMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtMarkdownMsg;
import com.inspur.emmcloud.ui.chat.DisplayTxtPlainMsg;
import com.inspur.emmcloud.ui.chat.UnReadDetailActivity;
import com.inspur.emmcloud.ui.chat.selectabletext.SelectableTextHelper;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenmch on 2017/11/10.
 */

public class ChannelMessageAdapter extends RecyclerView.Adapter<ChannelMessageAdapter.ViewHolder> {

    private static final Integer MAX_MULTI_SELECT_COUNT = 100;
    private Activity context;
    private List<UIMessage> UIMessageList = new ArrayList<>();
    private MyItemClickListener mItemClickListener;
    private String channelType;
    private String membersDetail; // 群成员信息，包括昵称
    private ECMChatInputMenu chatInputMenu;
    private ArrayList<String> mExceptSelfMemberList = new ArrayList<>();
    private String uid = BaseApplication.getInstance().getUid();
    private boolean serviceConversation = false;
    private boolean mMultipleSelect;

    private Set<UIMessage> mSelectedMessages = new HashSet<>();
    private JSONArray membersDetailArray;


    public ChannelMessageAdapter(Activity context, String channelType, ECMChatInputMenu chatInputMenu, ArrayList<String> memberList, boolean isServiceCoversation, String membersDetail) {
        this.context = context;
        this.channelType = channelType;
        this.membersDetail = membersDetail;
        membersDetailArray = JSONUtils.getJSONArray(membersDetail, new JSONArray());
        this.chatInputMenu = chatInputMenu;
        this.serviceConversation = isServiceCoversation;
        List<ContactUser> totalList = ContactUserCacheUtils.getContactUserListById(memberList);
        for (ContactUser contact : totalList) {
            mExceptSelfMemberList.add(contact.getId());
        }
        mExceptSelfMemberList.remove(uid);

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

    public void updateMembersDetail(String membersDetail) {
        this.membersDetail = membersDetail;
        membersDetailArray = JSONUtils.getJSONArray(membersDetail, new JSONArray());
        notifyDataSetChanged();
    }

    public void updateMemberList(ArrayList<String> memberList) {
        if (memberList == null) {
            return;
        }
        mExceptSelfMemberList.clear();
        List<ContactUser> totalList = ContactUserCacheUtils.getContactUserListById(memberList);
        for (ContactUser contact : totalList) {
            mExceptSelfMemberList.add(contact.getId());
        }
        mExceptSelfMemberList.remove(uid);
        notifyDataSetChanged();
    }

    public void setMessageList(List<UIMessage> UIMessageList) {
        this.UIMessageList.clear();
        this.UIMessageList.addAll(UIMessageList);
    }


    public List<UIMessage> getAdapterUIMessageList() {
        return this.UIMessageList;
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
        if (StringUtils.isBlank(uiMessage.getMessage().getRecallFrom())) {
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
                holder.sendStatusLayout.setVisibility(isMyMsg ? (mMultipleSelect ? View.GONE : View.INVISIBLE) : View.GONE);
                RelativeLayout.LayoutParams cardLayoutParams = (RelativeLayout.LayoutParams) holder.cardLayout.getLayoutParams();
                cardLayoutParams.leftMargin = DensityUtil.dip2px(mMultipleSelect ? 15 : 5);
                holder.cardLayout.setLayoutParams(cardLayoutParams);
            }
            holder.sendStatusLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.onMessageResendClick(uiMessage);
                }
            });
        } else {
            holder.sendStatusLayout.setVisibility(View.GONE);
        }

    }

    /**
     * 显示卡片的内容
     *
     * @param holder
     */
    private void showCardLayout(final ViewHolder holder, final UIMessage uiMessage) {
        Message message = uiMessage.getMessage();
        boolean isMyMsg = message.getFromUser().equals(
                MyApplication.getInstance().getUid());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.cardParentLayout.getLayoutParams();
        //此处实际执行params.removeRule();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        params.addRule(RelativeLayout.ALIGN_LEFT, 0);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
        if (StringUtils.isBlank(message.getRecallFrom())) {
            params.addRule(isMyMsg ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_LEFT);
        } else {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        holder.cardParentLayout.setLayoutParams(params);
        holder.checkbox.setVisibility(mMultipleSelect ? View.VISIBLE : View.GONE);
        if (supportMultiSelect(uiMessage)) {
            holder.checkbox.setImageResource(mSelectedMessages.contains(uiMessage) ? R.drawable.ic_select_yes :
                    (DarkUtil.isDarkTheme() ? R.drawable.ic_select_no_dark : R.drawable.ic_select_no));
        } else {
            holder.checkbox.setImageResource(DarkUtil.isDarkTheme() ? R.drawable.ic_not_select_dark : R.drawable.ic_not_select);
        }

        holder.cardLayout.removeAllViewsInLayout();
        holder.cardLayout.removeAllViews();
        View cardContentView = null;
        boolean bottomViewUsed;
        if (StringUtils.isBlank(uiMessage.getMessage().getRecallFrom())) {
            final String type = message.getType();
            SelectableTextHelper mSelectableTextHelper = null;
            switch (type) {
                case Message.MESSAGE_TYPE_TEXT_WHISPER:
                case Message.MESSAGE_TYPE_TEXT_BURN:
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                    cardContentView = DisplayTxtPlainMsg.getView(context,
                            message, TextUtils.isEmpty(membersDetail) ? null : membersDetailArray);
                    mSelectableTextHelper = new SelectableTextHelper.Builder((TextView) cardContentView.findViewById(R.id.tv_content))
                            .setSelectedColor(isMyMsg ? context.getResources().getColor(R.color.selected_send_msg_bg) : context.getResources().getColor(R.color.selected_receive_msg_bg))
                            .setCursorHandleSizeInDp(20)
                            .setCursorHandleColor(context.getResources().getColor(R.color.cursor_handle_color))
                            .build();
                    break;
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    cardContentView = DisplayTxtMarkdownMsg.getView(context,
                            message, uiMessage.getMarkDownLinkList());
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
                case Message.MESSAGE_TYPE_EXTENDED_SELECTED:
                    cardContentView = DisplayExtendedDecideMsg.getView(message, context);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VIDEO:
                    cardContentView = DisplayMediaVideoMsg.getView(context, uiMessage);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    cardContentView = DisplayMediaImageMsg.getView(context, uiMessage);
                    break;
                case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                    if (serviceConversation) {
                        cardContentView = DisplayServiceCommentTextPlainMsg.getView(context, message);
                    } else {
                        // 老版
//                        cardContentView = DisplayCommentTextPlainMsg.getView(context, message);
                        // 新版回复View
                        cardContentView = DisplayCommentNewMsg.getView(context, message, TextUtils.isEmpty(membersDetail) ? null : membersDetailArray);
                    }
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                    cardContentView = DisplayExtendedLinksMsg.getView(context, message);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VOICE:
                    cardContentView = DisplayMediaVoiceMsg.getView(context, uiMessage, mItemClickListener);
                    break;
                case Message.MESSAGE_TYPE_COMPLEX_MESSAGE:
                    cardContentView = DisplayMultiMsg.getView(context, uiMessage);
                    break;
                default:
                    cardContentView = DisplayResUnknownMsg.getView(context, isMyMsg);
                    break;
            }
            final SelectableTextHelper finalMSelectableTextHelper = mSelectableTextHelper;
            cardContentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mItemClickListener != null) {
                        if (Message.MESSAGE_TYPE_TEXT_PLAIN.equals(type)) {
                            mItemClickListener.onTxtItemLongClick(view, uiMessage, finalMSelectableTextHelper);
                            return true;
                        }
                        mItemClickListener.onCardItemLongClick(view, uiMessage);
                    }
                    return true;
                }
            });
            cardContentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!dealMultiClick(holder.checkbox, uiMessage)) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onCardItemClick(view, uiMessage);
                        }
                    }
                }
            });
            //悄悄话、阅后即焚 标识
            List<String> whispers = message.getMsgContentTextPlain().getWhisperUsers();
            String msgType = message.getMsgContentTextPlain().getMsgType();
            boolean isBurnMsg = msgType.equals(Message.MESSAGE_TYPE_TEXT_BURN);
            if (!whispers.isEmpty() || isBurnMsg) {
                int iconDrawableId = whispers.isEmpty() ? R.drawable.icon_chat_burn : R.drawable.icon_chat_whisper;
                Drawable iconDrawable = context.getResources().getDrawable(iconDrawableId);
                iconDrawable.setBounds(0, 0, iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight());
                int bgDrawableId = whispers.isEmpty() ? R.drawable.bg_corner_burn_r10 : R.drawable.bg_corner_whisper_r10;
                int textColor = whispers.isEmpty() ? R.color.color_burn_text : R.color.color_whisper_text;
                if (isMyMsg) {
                    holder.bottomInfoTypeLeft.setVisibility(View.GONE);
                    holder.bottomInfoTypeRight.setVisibility(View.VISIBLE);
                    holder.bottomInfoTypeRight.setCompoundDrawables(iconDrawable, null, null, null);
                    holder.bottomInfoTypeRight.setBackgroundResource(bgDrawableId);
                    holder.bottomInfoTypeRight.setTextColor(context.getResources().getColor(textColor));
                    holder.bottomInfoTypeRight.setText(whispers.isEmpty() ? context.getString(R.string.read_disappear) : context.getString(R.string.chat_whisper, createChannelGroupName(whispers)));
                } else {
                    holder.bottomInfoTypeRight.setVisibility(View.GONE);
                    holder.bottomInfoTypeLeft.setVisibility(View.VISIBLE);
                    holder.bottomInfoTypeLeft.setCompoundDrawables(iconDrawable, null, null, null);
                    holder.bottomInfoTypeLeft.setBackgroundResource(bgDrawableId);
                    holder.bottomInfoTypeLeft.setTextColor(context.getResources().getColor(textColor));
                    holder.bottomInfoTypeLeft.setText(whispers.isEmpty() ? context.getString(R.string.read_disappear) : context.getString(R.string.voice_whisper));
                }
                bottomViewUsed = true;
            } else {
                holder.bottomInfoTypeRight.setVisibility(View.GONE);
                holder.bottomInfoTypeLeft.setVisibility(View.GONE);
                bottomViewUsed = false;
            }
        } else {
            cardContentView = DisplayRecallMsg.getView(context, uiMessage);
            //撤回5分钟以内的文本消息显示撤回选线IG
            holder.bottomInfoTypeRight.setVisibility(View.GONE);
            holder.bottomInfoTypeLeft.setVisibility(View.GONE);
            bottomViewUsed = true;
            cardContentView.findViewById(R.id.tv_edit_again).setVisibility((uiMessage.getMessage().getType()
                    .equals(Message.MESSAGE_TYPE_TEXT_PLAIN)
                    && (System.currentTimeMillis() - uiMessage.getMessage().getCreationDate() <= 5 * 60 * 1000)
                    && uiMessage.getMessage().getFromUser().equals(BaseApplication.getInstance().getUid())) ? View.VISIBLE : View.GONE);
            cardContentView.findViewById(R.id.tv_edit_again).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onCardItemClick(view, uiMessage);
                    }
                }
            });
        }
        if (serviceConversation) bottomViewUsed = true;
        holder.cardLayout.addView(cardContentView);

        //处理已读未读
        if (bottomViewUsed || !NetUtils.isNetworkConnected(context)) {
            holder.unreadText.setVisibility(View.GONE);
            return;
        }
        Map<String, Set<String>> statesMap = uiMessage.getStatesMap();
        Set<String> readList = statesMap.get(ChannelMessageStates.READ);
        Set<String> sentList = statesMap.get(ChannelMessageStates.SENT);
        Set<String> deliveredList = statesMap.get(ChannelMessageStates.DELIVERED);
        int readSize = readList == null ? 0 : readList.size();
        int sentSize = sentList == null ? 0 : sentList.size();
        int deliveredSize = deliveredList == null ? 0 : deliveredList.size();
        int allSize = readSize + sentSize + deliveredSize;
        if (allSize == 0 || !uiMessage.getMessage().getFromUser().equals(uid) || uiMessage.getSendStatus() != 1) {
            holder.unreadText.setVisibility(View.GONE);
        } else if (allSize == 1) {
            holder.unreadText.setVisibility(View.VISIBLE);
            holder.unreadText.setText(context.getResources().getString(readSize == 1 ? R.string.read : R.string.unread));
            holder.unreadText.setTextColor(Color.parseColor(readSize == 1 ? "#999999" : "#36A5F6"));
            holder.unreadText.setOnClickListener(null);
        } else if (allSize > 1) {
            holder.unreadText.setVisibility(View.VISIBLE);
            if (readSize >= allSize) {
                holder.unreadText.setText(context.getResources().getString(R.string.all_read));
                holder.unreadText.setTextColor(Color.parseColor("#999999"));
                holder.unreadText.setOnClickListener(null);
            } else {
                holder.unreadText.setText((sentSize + deliveredSize) + context.getResources().getString(R.string.left_unread));
                holder.unreadText.setTextColor(Color.parseColor("#36A5F6"));
                holder.unreadText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, UnReadDetailActivity.class);
                        intent.putExtra(UnReadDetailActivity.UI_MESSAGE, uiMessage);
                        intent.putStringArrayListExtra(UnReadDetailActivity.CONVERSATION_ALL_MEMBER, mExceptSelfMemberList);
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    /**
     * 展示消息发送时间
     *
     * @param holder
     * @param position
     */
    private void showMsgSendTime(ViewHolder holder, UIMessage UIMessage, int position) {
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

    private String createChannelGroupName(List<String> uidList) {
        if (uidList.isEmpty()) return "";
        List<ContactUser> contactUsers = ContactUserCacheUtils.getContactUserListById(uidList);
        StringBuilder nameBuilder = new StringBuilder();
        int length = Math.min(4, uidList.size());
        nameBuilder.append(contactUsers.get(0).getName());
        for (int i = 1; i < length; i++) {
            String name = "";
            name = contactUsers.get(i).getName();
            nameBuilder.append("、").append(name);
        }
        if (uidList.size() > 4) {
            nameBuilder.append("...");
        }
        return nameBuilder.toString();
    }

    /**
     * 展示用户名称
     *
     * @param holder
     */
    private void showUserName(ViewHolder holder, UIMessage uIMessage) {
        if (channelType.equals("GROUP") && !uIMessage.getMessage().getFromUser().equals(
                MyApplication.getInstance().getUid()) && StringUtils.isBlank(uIMessage.getMessage().getRecallFrom())) {
            holder.senderNameText.setVisibility(View.VISIBLE);
            String username = "";
            if (!TextUtils.isEmpty(membersDetail)) {
                String fromUser = uIMessage.getMessage().getFromUser();
                for (int i = 0; i < membersDetailArray.length(); i++) {
                    JSONObject obj = JSONUtils.getJSONObject(membersDetailArray, i, new JSONObject());
                    if (fromUser.equals(JSONUtils.getString(obj, "user", ""))) {
                        String nickname = JSONUtils.getString(obj, "nickname", "");
                        if (TextUtils.isEmpty(nickname)) {
                            username = uIMessage.getSenderName();
                            holder.senderNameText.setText(username);
                        } else {
                            username = nickname;
                            holder.senderNameText.setText(nickname);
                        }
                        break;
                    }
                }
                if (TextUtils.isEmpty(username)) {
                    username = uIMessage.getSenderName();
                    holder.senderNameText.setText(username);
                }
            } else {
                holder.senderNameText.setText(uIMessage.getSenderName());
            }
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
        final String fromUser = UImessage.getMessage().getFromUser();
        boolean isMyMsg = MyApplication.getInstance().getUid().equals(fromUser);
        if (StringUtils.isBlank(UImessage.getMessage().getRecallFrom())) {
            holder.senderPhotoImgRight.setVisibility(isMyMsg ? View.VISIBLE : (mMultipleSelect ? View.GONE : View.INVISIBLE));
            holder.senderPhotoImgLeft.setVisibility(isMyMsg ? View.GONE : View.VISIBLE);
        } else {
            holder.senderPhotoImgRight.setVisibility(View.GONE);
            holder.senderPhotoImgLeft.setVisibility(View.GONE);
        }

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
                    chatInputMenu.addMentions(fromUser, UImessage.getSenderName(), false, null);
                }
                return true;
            }
        });
    }

    public void updatesChannelMessageState(String messageId, Map<String, Set<String>> map) {
        for (int i = UIMessageList.size() - 1; i >= 0; i--) {
            UIMessage uiMessage = UIMessageList.get(i);
            if (uiMessage.getId().equals(messageId)) {
                Set<String> newReadSet = map.get(ChannelMessageStates.READ);
                if (newReadSet == null || newReadSet.size() == 0 || uiMessage.getStatesMap() == null) {
                    break;
                }
                String newReadId = newReadSet.iterator().next();
                Set<String> oldReadList = uiMessage.getStatesMap().get(ChannelMessageStates.READ);
                Set<String> oldSentList = uiMessage.getStatesMap().get(ChannelMessageStates.SENT);
                Set<String> oldDeliveredList = uiMessage.getStatesMap().get(ChannelMessageStates.DELIVERED);
                if (oldSentList != null) {
                    oldSentList.remove(newReadId);
                }
                if (oldDeliveredList != null) {
                    oldDeliveredList.remove(newReadId);
                }
                if (oldReadList == null) {
                    oldReadList = new HashSet<>();
                    uiMessage.getStatesMap().put(ChannelMessageStates.READ, oldReadList);
                }
                if (!oldReadList.contains(newReadId)) {
                    oldReadList.add(newReadId);
                    notifyItemChanged(i);
                }
                break;
            }
        }
    }

    public void toggleMultipleSelect(boolean b) {
        if (b && !mMultipleSelect) {
            mSelectedMessages.clear();
        }
        mMultipleSelect = b;
        notifyDataSetChanged();
    }

    private boolean supportMultiSelect(UIMessage uiMessage) {
        Message message = uiMessage.getMessage();
        String type = message.getType();
        if (!StringUtils.isBlank(uiMessage.getMessage().getRecallFrom()) || uiMessage.getSendStatus() != 1) {
            return false;
        }
        boolean messageTypeSupport = Message.MESSAGE_TYPE_MEDIA_IMAGE.equals(type) || Message.MESSAGE_TYPE_FILE_REGULAR_FILE.equals(type)
                || Message.MESSAGE_TYPE_TEXT_MARKDOWN.equals(type) || Message.MESSAGE_TYPE_EXTENDED_LINKS.equals(type)
                || Message.MESSAGE_TYPE_MEDIA_VIDEO.equals(type);
        if (messageTypeSupport) {
            return true;
        }
        if (type.equals(Message.MESSAGE_TYPE_TEXT_PLAIN)) {
            String textMsgType = message.getMsgContentTextPlain().getMsgType();
            if (Message.MESSAGE_TYPE_TEXT_BURN.equals(textMsgType)) {
                return false;
            }
            return message.getMsgContentTextPlain().getWhisperUsers().isEmpty();
        }
        return false;
    }

    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {

        boolean onCardItemLongClick(View view, UIMessage uiMessage);

        //SelectableText
        void onTxtItemLongClick(View view, UIMessage uiMessage, SelectableTextHelper selectableTextHelper);

        void onCardItemClick(View view, UIMessage uiMessage);

        void onCardItemLayoutClick(View view, UIMessage uiMessage);

        void onMessageResend(UIMessage uiMessage, View view);

        void onAdapterDataSizeChange();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public RelativeLayout cardLayout;
        public TextView senderNameText;
        public ImageView senderPhotoImgLeft;
        public ImageView senderPhotoImgRight;
        public RelativeLayout sendStatusLayout;
        public ImageView sendFailImg;
        public CustomLoadingView sendingLoadingView;
        public TextView sendTimeText;
        public TextView bottomInfoTypeLeft;
        public TextView bottomInfoTypeRight;
        public RelativeLayout cardParentLayout;
        public ImageView checkbox;
        private MyItemClickListener mListener;
        public TextView unreadText;

        public ViewHolder(View view, MyItemClickListener myItemClickListener) {
            super(view);
            //将全局的监听赋值给接口
            this.mListener = myItemClickListener;

            cardLayout = (RelativeLayout) view
                    .findViewById(R.id.bll_card);
            cardLayout.setOnClickListener(this);
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
            bottomInfoTypeLeft = (TextView) view
                    .findViewById(R.id.chat_msg_bottom_text_left);
            bottomInfoTypeRight = (TextView) view
                    .findViewById(R.id.chat_msg_bottom_text_right);
            unreadText = (TextView) view
                    .findViewById(R.id.chat_msg_unread_text);
            cardParentLayout = (RelativeLayout) view.findViewById(R.id.card_parent_layout);
            checkbox = view.findViewById(R.id.chat_msg_checkbox);
            itemView.setOnClickListener(this);

        }

        /**
         * 实现OnClickListener接口重写的方法
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            if (!dealMultiClick((ImageView) v.findViewById(R.id.chat_msg_checkbox), UIMessageList.get(getAdapterPosition()))) {
                int position = getAdapterPosition();
                if (mItemClickListener != null && position != -1) {
                    mItemClickListener.onCardItemLayoutClick(v, UIMessageList.get(getAdapterPosition()));
                }
            }
        }

        public void onMessageResendClick(UIMessage uiMessage) {
            if (mListener != null) {
                mListener.onMessageResend(uiMessage, sendFailImg);
            }

        }
    }

    public Set<UIMessage> getSelectedMessages() {
        return mSelectedMessages;
    }


    private boolean dealMultiClick(ImageView checkbox, UIMessage uiMessage) {
        if (mMultipleSelect) {
            if (supportMultiSelect(uiMessage)) {
                if (mSelectedMessages.contains(uiMessage)) {
                    checkbox.setImageResource(R.drawable.ic_select_no);
                    mSelectedMessages.remove(uiMessage);
                } else {
                    if (mSelectedMessages.size() >= MAX_MULTI_SELECT_COUNT) {
                        RemindDialog.buildDialog(context, context.getString(R.string.multi_select_max_tip, MAX_MULTI_SELECT_COUNT + "")).show();
                        return true;
                    }
                    checkbox.setImageResource(R.drawable.ic_select_yes);
                    mSelectedMessages.add(uiMessage);
                }
            }
            return true;
        }
        return false;
    }
}