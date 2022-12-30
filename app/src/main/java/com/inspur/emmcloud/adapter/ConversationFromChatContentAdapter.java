package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.ConversationWithMessageNum;
import com.inspur.emmcloud.bean.chat.SearchHolder;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.util.privates.ConversationOrContactGetIconUtil;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/12/23.
 */

public class ConversationFromChatContentAdapter extends BaseAdapter {

    private List<ConversationWithMessageNum> conversationWithNumList = new ArrayList<>();
    private boolean isLimited = true;
    private Context mContext;

    public ConversationFromChatContentAdapter(Context context) {
        mContext = context;
    }


    public boolean getLimited() {
        return isLimited;
    }

    public void setLimited(boolean isLimited) {
        this.isLimited = isLimited;
    }

    public void setConversationList(List<ConversationWithMessageNum> conversationWithMessageNumList) {
        this.conversationWithNumList = conversationWithMessageNumList;
    }


    @Override
    public int getCount() {
        if (isLimited && conversationWithNumList.size() > 3) {
            return 3;
        } else {
            return conversationWithNumList.size();
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SearchHolder searchHolder = new SearchHolder();
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.communication_search_contact_item, null);
            searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
            searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
            searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
            view.setTag(searchHolder);
        } else {
            searchHolder = (SearchHolder) view.getTag();
        }
        Conversation conversation = null;
        if (conversationWithNumList.size() > 0) {
            conversation = conversationWithNumList.get(i).getConversation();
        }
        if (conversation != null && (conversation.getType().equals(Conversation.TYPE_GROUP))) {
            SearchModel searchModel = conversation.conversation2SearchModel();
            ConversationOrContactGetIconUtil.displayImg(searchModel, searchHolder.headImageView);
            searchHolder.nameTextView.setText(searchModel.getName().toString());
            String string = BaseApplication.getInstance().getString(R.string.chat_contact_related_message, conversationWithNumList.get(i).getMessageNum());
            searchHolder.detailTextView.setText(string);
            searchHolder.detailTextView.setVisibility(View.VISIBLE);
        }
        if (conversation != null && (conversation.getType().equals(Conversation.TYPE_CAST))) {
            UIConversation uiConversation = new UIConversation(conversation);
            searchHolder.nameTextView.setText(uiConversation.getTitle());
            ImageDisplayUtils.getInstance().displayImage(searchHolder.headImageView, uiConversation.getIcon(), R.drawable.icon_person_default);
            String string = BaseApplication.getInstance().getString(R.string.chat_contact_related_message, conversationWithNumList.get(i).getMessageNum());
            searchHolder.detailTextView.setText(string);
            searchHolder.detailTextView.setVisibility(View.VISIBLE);
        }
        if (conversation != null && (conversation.getType().equals(Conversation.TYPE_TRANSFER))) {
            searchHolder.nameTextView.setText(BaseApplication.getInstance().getString(R.string.chat_file_transfer));
            ImageDisplayUtils.getInstance().displayImageByTag(searchHolder.headImageView, conversation.getAvatar(), R.drawable.design3_icon_transfer);
            String string = BaseApplication.getInstance().getString(R.string.chat_contact_related_message, conversationWithNumList.get(i).getMessageNum());
            searchHolder.detailTextView.setText(string);
            searchHolder.detailTextView.setVisibility(View.VISIBLE);
        }
        if (conversation != null && conversation.getType().equals(Conversation.TYPE_DIRECT)) {
            String icon = DirectChannelUtils.getDirectChannelIcon(MyApplication.getInstance(), conversation.getName());
            ImageDisplayUtils.getInstance().displayImageByTag(searchHolder.headImageView, icon, R.drawable.icon_person_default);
            searchHolder.nameTextView.setText(conversation.getShowName());
            String string = BaseApplication.getInstance().getString(R.string.chat_contact_related_message, conversationWithNumList.get(i).getMessageNum());
            searchHolder.detailTextView.setText(string);
            searchHolder.detailTextView.setVisibility(View.VISIBLE);
        }
        //刷新数据
        return view;
    }
}
