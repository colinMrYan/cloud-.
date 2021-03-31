package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.bean.chat.InputTypeBean;

import java.util.ArrayList;
import java.util.List;


public class MsgInputAddItemAdapter extends BaseAdapter {

    private Context context;
    private List<InputTypeBean> inputTypeBeanList = new ArrayList<>();

    public MsgInputAddItemAdapter(Context context) {
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public MsgInputAddItemAdapter(Context context, List<InputTypeBean> inputTypeBeanList) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.inputTypeBeanList = inputTypeBeanList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return inputTypeBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        convertView = LayoutInflater.from(context).inflate(R.layout.msg_add_item_view, null);
        ((ImageView) convertView.findViewById(R.id.img)).setImageResource(inputTypeBeanList.get(position).getInputTypeIcon());
        ((TextView) convertView.findViewById(R.id.text)).setText(inputTypeBeanList.get(position).getInputTypeName());
        return convertView;
    }

    /**
     * 更新聊天页面输入框添加功能显示列表
     */
    public void updateGridView(List<InputTypeBean> functionList) {
        this.inputTypeBeanList = functionList;
        notifyDataSetChanged();
    }

}
