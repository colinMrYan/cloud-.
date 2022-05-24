package com.inspur.emmcloud.ui.chat.mvp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import java.util.List;

/**
 * Date：2022/5/4
 * Author：wang zhen
 * Description 转发多人时dialog
 */
public class ConversationSendMultiDialogAdapter extends BaseAdapter {

    private Context context;
    private List<MessageForwardMultiBean> searchModelList;

    public ConversationSendMultiDialogAdapter(Context context, List<MessageForwardMultiBean> searchModelList) {
        this.context = context;
        this.searchModelList = searchModelList;
    }

    @Override
    public int getCount() {
        return searchModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.conversation_send_multi_item, null);
            holder.headImage = view.findViewById(R.id.iv_members_head);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        MessageForwardMultiBean conversation = searchModelList.get(position);
        int defaultIcon = conversation.getType().equals(Conversation.TYPE_GROUP) ?
                R.drawable.icon_channel_group_default : R.drawable.icon_person_default;
        String imageUrl = CommunicationUtils.getHeadUrl(conversation);
        holder.headImage.setTag(imageUrl);
        ImageDisplayUtils.getInstance().displayImageByTag(holder.headImage, imageUrl, defaultIcon);
        return view;
    }

    class ViewHolder {
        private CircleTextImageView headImage;
    }
}
