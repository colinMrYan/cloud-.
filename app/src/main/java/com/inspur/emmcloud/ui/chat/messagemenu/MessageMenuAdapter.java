package com.inspur.emmcloud.ui.chat.messagemenu;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

import java.util.List;

public class MessageMenuAdapter extends RecyclerView.Adapter {
    private List<MessageMenuItem> dataList;
    private Context context;
    private MessageMenuPopupWindow.PopItemClickListener mPopItemClickListener;

    public MessageMenuAdapter(Context context, List<MessageMenuItem> dataList) {
        this.dataList = dataList;
        this.context = context;
    }
    
    

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = View.inflate(context, R.layout.message_menu_pop_item_layout, null);
        return new PopAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {
        PopAdapterViewHolder holder = (PopAdapterViewHolder) viewHolder;
        final MessageMenuItem item = dataList.get(i);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPopItemClickListener != null){
                    mPopItemClickListener.onPopItemClick(item);
                }
            }
        });
        holder.imageView.setImageResource(item.resourceImageId);
        holder.textView.setText(item.text);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setPopItemClickListener(MessageMenuPopupWindow.PopItemClickListener itemClickListener) {
        mPopItemClickListener = itemClickListener;
    }

    private static class PopAdapterViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView textView;
        ImageView imageView;

        public PopAdapterViewHolder(View view) {
            super(view);
            this.itemView = view;
            this.textView = view.findViewById(R.id.pop_item_text);
            this.imageView = view.findViewById(R.id.pop_item_image);
        }
    }
}
