package com.inspur.emmcloud.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.bean.chat.SearchMultiHolder;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/12/23.
 */

public class PrivateChatMultiAdapter extends BaseAdapter {
    private List<Conversation> conversationList = new ArrayList<>();
    private Context mContext;
    private List<MessageForwardMultiBean> selectList = new ArrayList<>();

    public PrivateChatMultiAdapter(@NonNull Context context) {
        mContext = context;
    }

    public void setConversationList(List<Conversation> contentList) {
        this.conversationList = contentList;
    }

    public void setSelectList(List<MessageForwardMultiBean> selectList) {
        this.selectList = selectList;
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
        SearchMultiHolder searchHolder = new SearchMultiHolder();
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.communication_search_contact_multi_item, null);
            searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
            searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
            searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
            searchHolder.selectImage = view.findViewById(R.id.selected_img);
            view.setTag(searchHolder);
        } else {
            searchHolder = (SearchMultiHolder) view.getTag();
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
                    icon = "drawable://" + R.drawable.design3_icon_transfer;
                    break;
            }
            ImageDisplayUtils.getInstance().displayImageByTag(searchHolder.headImageView, icon,
                    ResourceUtils.getResValueOfAttr(mContext, R.attr.design3_icon_person_default));
            searchHolder.nameTextView.setText(conversation.getShowName());
            CommunicationUtils.setUserDescText(conversation.conversation2SearchModel(), searchHolder.detailTextView);
            MessageForwardMultiBean bean = new MessageForwardMultiBean(conversation.getId(),
                    conversation.getName(), conversation.getType(), conversation.getAvatar(), "");
            if (selectList.contains(bean)) {
                searchHolder.selectImage.setImageResource(R.drawable.ic_select_yes);
            } else {
                searchHolder.selectImage.setImageResource(R.drawable.ic_select_no);
            }
        }
        //刷新数据
        return view;
    }
}
