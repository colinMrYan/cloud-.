package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.mail.Mail;
import com.inspur.emmcloud.bean.appcenter.mail.MailAttachment;

/**
 * Created by chenmch on 2018/12/27.
 */

public class MailAttachmentListAdapter extends BaseAdapter {
    private Mail mail;
    private Context context;
    public MailAttachmentListAdapter(Context context, Mail mail){
        this.context = context;
        this.mail = mail;
    }

    @Override
    public int getCount() {
        return mail.getMailAttachmentList().size();
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
        MailAttachment mailAttachment = mail.getMailAttachmentList().get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.mail_attachment_item_view,null);
        TextView attachmentNameText = convertView.findViewById(R.id.tv_name);
        attachmentNameText.setText(mailAttachment.getName());
        TextView downloadText = convertView.findViewById(R.id.tv_download);
        downloadText.setText(R.string.open);
        return convertView;
    }
}
