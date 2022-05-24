package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.bean.chat.SearchMultiHolder;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationOrContactGetIconUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/12/23.
 */

public class GroupOrContactMultiAdapter extends BaseAdapter {

    private List<SearchModel> contentList = new ArrayList<>();
    private List<MessageForwardMultiBean> selectList = new ArrayList<>();

    private Context mContext;

    public GroupOrContactMultiAdapter(Context context) {
        mContext = context;
    }

    public void setContentList(List<SearchModel> contentList) {
        this.contentList = contentList;
    }

    public void setSelectList(List<MessageForwardMultiBean> selectList) {
        this.selectList = selectList;
    }

    @Override
    public int getCount() {
        return contentList.size();
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
        SearchModel searchModel = contentList.get(i);
        if (searchModel != null) {
            ConversationOrContactGetIconUtil.displayImg(searchModel, searchHolder.headImageView);
            searchHolder.nameTextView.setText(searchModel.getName().toString());
            CommunicationUtils.setUserDescText(searchModel, searchHolder.detailTextView);
            boolean isSelf = searchModel.getId().equals(BaseApplication.getInstance().getUid());
            MessageForwardMultiBean bean;
            if (searchModel.getType().equals(SearchModel.TYPE_USER)) {
                bean = new MessageForwardMultiBean("",
                        searchModel.getName(), searchModel.getType(), searchModel.getIcon(), searchModel.getId());
            } else {
                bean = new MessageForwardMultiBean(searchModel.getId(),
                        searchModel.getName(), searchModel.getType(), searchModel.getIcon(), "");
            }
            // 不能选自己，最近会话列表不可能有自己，这里可以不判断
            if (searchModel.getType().equals(SearchModel.TYPE_USER) && isSelf) {
                searchHolder.selectImage.setImageResource(R.drawable.ic_select_not_cancel);
            } else if (selectList.contains(bean)) {
                searchHolder.selectImage.setImageResource(R.drawable.ic_select_yes);
            } else {
                searchHolder.selectImage.setImageResource(R.drawable.ic_select_no);
            }
        }

        //刷新数据
        return view;
    }
}
