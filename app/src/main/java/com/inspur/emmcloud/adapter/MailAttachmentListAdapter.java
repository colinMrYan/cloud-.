package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;

/**
 * Created by chenmch on 2018/12/27.
 */

public class MailAttachmentListAdapter extends BaseAdapter {
    private Context context;
    public MailAttachmentListAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return 0;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.mail_attachment_item_view,null);
        TextView attachmentNameText = (TextView)convertView.findViewById(R.id.tv_name);
        attachmentNameText.setText("山东健康大讲堂报名表.xlsx");
        return convertView;
    }
}
