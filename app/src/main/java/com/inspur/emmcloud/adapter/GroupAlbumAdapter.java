package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2019/1/21.
 */

public class GroupAlbumAdapter extends RecyclerView.Adapter<GroupAlbumAdapter.AlbumViewHolder>{

    private Context context;
    private List<Map<String,List<String>>> imageList = new ArrayList<>();
    public GroupAlbumAdapter(Context context,List<Map<String,List<String>>> imageList){
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView appDetailImg;

        public AlbumViewHolder(View itemView) {
            super(itemView);
        }
    }
}
