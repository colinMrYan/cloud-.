package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.CircleTextImageView;

/**
 * Created by chenmch on 2018/12/21.
 */

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.ViewHolder> {
    private Context context;

    public MailAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mail_list_item_view, parent, false);
        ViewHolder holder = new ViewHolder(view, null);
        return holder;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private CircleTextImageView senderPhotoImg;
        private TextView senderNameText;
        private ImageView encryptFlagImg;
        private ImageView signFlagImg;
        private TextView timeText;
        private TextView topicText;
        private AdapterListener adapterListener;

        public ViewHolder(View convertView, AdapterListener adapterListener) {
            super(convertView);
            this.adapterListener = adapterListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            senderPhotoImg = (CircleTextImageView) convertView.findViewById(R.id.iv_mail_sender_photo);
            senderNameText = (TextView) convertView.findViewById(R.id.tv_sender_name);
            timeText = (TextView) convertView.findViewById(R.id.tv_time);
            topicText = (TextView) convertView.findViewById(R.id.tv_topic);
            encryptFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_encrypt);
            signFlagImg = (ImageView) convertView.findViewById(R.id.iv_flag_sign);
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

    /**
     * 创建一个回调接口
     */
    public interface AdapterListener {
        void onItemClick(View view, int position);

        boolean onItemLongClick(View view, int position);
    }
}
