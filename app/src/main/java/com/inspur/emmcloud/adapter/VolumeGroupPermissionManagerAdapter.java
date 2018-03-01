package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by yufuchang on 2018/3/1.
 */

public class VolumeGroupPermissionManagerAdapter extends RecyclerView.Adapter<VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerAdapterHolder>{

    private LayoutInflater inflater;

    public VolumeGroupPermissionManagerAdapter(Context context){
        inflater = LayoutInflater.from(context);
    }
    @Override
    public VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerAdapterHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class VolumeGroupPermissionManagerAdapterHolder extends RecyclerView.ViewHolder {
        ImageView recommendAppImg;
        TextView groupNameText;
        TextView permissionText;

        public VolumeGroupPermissionManagerAdapterHolder(View itemView) {
            super(itemView);
        }
    }
}
