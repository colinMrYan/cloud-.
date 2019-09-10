package com.inspur.emmcloud.mail.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.bean.MailAttachment;

import java.util.List;


/**
 * Created by chenmch on 2018/12/27.
 */

public class MailAttachmentListAdapter extends BaseAdapter {
    private List<MailAttachment> mailAttachmentList;
    private Context context;

    public MailAttachmentListAdapter(Context context, List<MailAttachment> mailAttachmentList) {
        this.context = context;
        this.mailAttachmentList = mailAttachmentList;
    }

    @Override
    public int getCount() {
        return mailAttachmentList.size();
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
        MailAttachment mailAttachment = mailAttachmentList.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.mail_attachment_item_view, null);
        TextView attachmentNameText = convertView.findViewById(R.id.tv_name);
        attachmentNameText.setText(mailAttachment.getName());
        return convertView;
    }
}
