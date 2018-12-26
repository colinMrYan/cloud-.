package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.mail.Mail;
import com.inspur.emmcloud.bean.appcenter.mail.MailFolder;
import com.inspur.emmcloud.util.privates.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/12/21.
 */

public class MailListAdapter extends BaseAdapter {
    private Context context;
    private List<Mail> mailList = new ArrayList<>();
    private MailFolder currentRootMailFolder;

    public MailListAdapter(Context context) {
        this.context = context;
    }

    public void setMailList(List<Mail> mailList,MailFolder currentRootMailFolder){
        this.mailList = mailList;
        this.currentRootMailFolder = currentRootMailFolder;
    }

    public void clearMailList(){
        this.mailList.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mailList.size();
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
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.mail_list_item_view, null);
            viewHolder.titleText = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.timeText = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.topicText = (TextView) convertView.findViewById(R.id.tv_topic);
            viewHolder.encryptFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_encrypt);
            viewHolder.signFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_sign);
            viewHolder.unReadView = convertView.findViewById(R.id.v_unread);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Mail mail = mailList.get(position);
        int folderType = currentRootMailFolder.getFolderType();
        //当root文件夹为发件箱、已发送和草稿箱时title显示收件人，否则显示发件人
        if (folderType == 0 || folderType == 2 || folderType ==3){
            viewHolder.titleText.setText(mail.getDisplayTo());
        }else {
            viewHolder.titleText.setText(mail.getDisplaySender());
        }
        String time = TimeUtils.getDisplayTime(context,mail.getCreationTimestamp());
        viewHolder.topicText.setText(mail.getSubject());
        viewHolder.encryptFlagImg.setImageResource(mail.isEncrypted()?R.drawable.ic_mail_flag_encrypt_yes:R.drawable.ic_mail_flag_encrypt_no);
        viewHolder.unReadView.setVisibility(mail.isRead()?View.GONE:View.VISIBLE);
        viewHolder.timeText.setText(time);
        return convertView;
    }

    public static class ViewHolder{
       TextView titleText;
        ImageView encryptFlagImg;
        ImageView signFlagImg;
        TextView timeText;
        TextView topicText;
       View unReadView;
    }

//    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
////        private CircleTextImageView senderPhotoImg;
//
//
//        public ViewHolder(View convertView, AdapterListener adapterListener) {
//            super(convertView);
//            this.adapterListener = adapterListener;
//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
////            senderPhotoImg = (CircleTextImageView) convertView.findViewById(R.id.iv_mail_sender_photo);
//            titleText = (TextView) convertView.findViewById(R.id.tv_title);
//            timeText = (TextView) convertView.findViewById(R.id.tv_time);
//            topicText = (TextView) convertView.findViewById(R.id.tv_topic);
//            encryptFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_encrypt);
//            signFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_sign);
//            unReadView=convertView.findViewById(R.id.v_unread);
//        }
//    }

}
