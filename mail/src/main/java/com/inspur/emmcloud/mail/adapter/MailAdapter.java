package com.inspur.emmcloud.mail.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.bean.Mail;
import com.inspur.emmcloud.mail.bean.MailFolder;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenmch on 2018/12/21.
 */

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.ViewHolder> {
    private Context context;
    private AdapterListener adapterListener;
    private List<Mail> mailList = new ArrayList<>();
    private MailFolder currentRootMailFolder;

    public MailAdapter(Context context, AdapterListener adapterListener) {
        this.context = context;
        this.adapterListener = adapterListener;
    }

    public void setMailList(List<Mail> mailList, MailFolder currentRootMailFolder) {
        this.mailList = mailList;
        this.currentRootMailFolder = currentRootMailFolder;
    }

    public void clearMailList() {
        this.mailList.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mailList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Mail mail = mailList.get(position);
        int folderType = currentRootMailFolder.getFolderType();
        //当root文件夹为发件箱、已发送和草稿箱时title显示收件人，否则显示发件人
        if (folderType == 0 || folderType == 2 || folderType == 3) {
            holder.titleText.setText(mail.getDisplayTo());
        } else {
            holder.titleText.setText(mail.getDisplaySender());
        }
        String time = TimeUtils.getDisplayTime(context, mail.getCreationTimestamp());
        holder.topicText.setText(mail.getSubject());
        holder.encryptFlagImg.setImageResource(mail.isEncrypted() ? R.drawable.ic_mail_flag_encrypt_yes : R.drawable.ic_mail_flag_encrypt_no);
        holder.unReadView.setVisibility(mail.isRead() ? View.GONE : View.VISIBLE);
        holder.timeText.setText(time);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mail_list_item_view, parent, false);
        ViewHolder holder = new ViewHolder(view, adapterListener);
        return holder;
    }

    /**
     * 创建一个回调接口
     */
    public interface AdapterListener {
        void onItemClick(View view, int position);

        boolean onItemLongClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        //        private CircleTextImageView senderPhotoImg;
        private TextView titleText;
        private ImageView encryptFlagImg;
        private ImageView signFlagImg;
        private TextView timeText;
        private TextView topicText;
        private View unReadView;
        private AdapterListener adapterListener;

        public ViewHolder(View convertView, AdapterListener adapterListener) {
            super(convertView);
            this.adapterListener = adapterListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
//            senderPhotoImg = (CircleTextImageView) convertView.findViewById(R.id.iv_mail_sender_photo);
            titleText = (TextView) convertView.findViewById(R.id.tv_title);
            timeText = (TextView) convertView.findViewById(R.id.tv_time);
            topicText = (TextView) convertView.findViewById(R.id.tv_topic);
            encryptFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_encrypt);
            signFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_sign);
            unReadView = convertView.findViewById(R.id.v_unread);
        }

        @Override
        public void onClick(View v) {
            if (adapterListener != null) {
                adapterListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (adapterListener != null) {
                adapterListener.onItemLongClick(v, getAdapterPosition());
            }
            return false;
        }
    }
}
