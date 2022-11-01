package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.SearchHolder;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/12/23.
 */

public class PrivateChatAdapter extends BaseAdapter {
    private List<Conversation> conversationList = new ArrayList<>();
    private Context mContext;

    public PrivateChatAdapter(@NonNull Context context){
        mContext = context;
    }

    public void setConversationList(List<Conversation> contentList) {
        this.conversationList = contentList;
    }

    @Override
    public int getCount() {
        return conversationList.size();
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
        Conversation conversation = conversationList.get(i);
        if (conversation != null) {
            String icon = "";
            switch (conversation.getType()) {
                case Conversation.TYPE_CAST:
                    icon = DirectChannelUtils.getRobotIcon(MyApplication.getInstance(), conversation.getName());
                    break;
                case Conversation.TYPE_DIRECT:
                    icon = DirectChannelUtils.getDirectChannelIcon(MyApplication.getInstance(), conversation.getName());
                    break;
                case Conversation.TYPE_TRANSFER:
                    icon = "drawable://" + R.drawable.ic_file_transfer;
                    break;
                default:
                    break;
            }
            ImageDisplayUtils.getInstance().displayImageByTag(searchHolder.headImageView, icon, R.drawable.icon_person_default);
            searchHolder.nameTextView.setText(conversation.getShowName().toString());
            CommunicationUtils.setUserDescText(conversation.conversation2SearchModel(), searchHolder.detailTextView);
        }
        //刷新数据
        return view;
    }
}
