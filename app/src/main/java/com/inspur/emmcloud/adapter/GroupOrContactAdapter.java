package com.inspur.emmcloud.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.chat.SearchHolder;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationOrContactGetIconUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/12/23.
 */

public class GroupOrContactAdapter extends BaseAdapter {

    private List<SearchModel> contentList = new ArrayList<>();

    public void setContentList(List<SearchModel> contentList) {
        this.contentList = contentList;
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
        SearchHolder searchHolder = new SearchHolder();
        if (view == null) {
            view = LayoutInflater.from(BaseApplication.getInstance()).inflate(R.layout.communication_search_contact_item, null);
            searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
            searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
            searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
            view.setTag(searchHolder);
        } else {
            searchHolder = (SearchHolder) view.getTag();
        }
        SearchModel searchModel = contentList.get(i);
        if (searchModel != null) {
            ConversationOrContactGetIconUtil.displayImg(searchModel, searchHolder.headImageView);
            searchHolder.nameTextView.setText(searchModel.getName().toString());
            CommunicationUtils.setUserDescText(searchModel, searchHolder.detailTextView);
        }
        //刷新数据
        return view;
    }
}
