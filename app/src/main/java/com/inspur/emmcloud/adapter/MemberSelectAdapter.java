package com.inspur.emmcloud.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by yufuchang on 2018/8/20.
 */

public class MemberSelectAdapter extends RecyclerView.Adapter<MemberSelectAdapter.MemberSelectHold>{

    @Override
    public MemberSelectHold onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MemberSelectHold holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class MemberSelectHold extends RecyclerView.ViewHolder{
        private ImageView imgUserHead;
        private TextView tvUserName;
        private ImageView imgSelected;
        public MemberSelectHold(View itemView) {
            super(itemView);
        }
    }
}
