package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.volume.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/3/1.
 */

public class VolumeGroupPermissionManagerAdapter extends RecyclerView.Adapter<VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerAdapterHolder> {

    private LayoutInflater inflater;
    private List<Group> groupList = new ArrayList<>();
    private VolumeGroupPermissionManagerInterface volumeGroupPermissionManagerInterface;
    private Context context;

    public VolumeGroupPermissionManagerAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.app_volume_permission_manager_item, null);
        VolumeGroupPermissionManagerAdapterHolder holder = new VolumeGroupPermissionManagerAdapterHolder(view);
        holder.groupNameText = (TextView) view.findViewById(R.id.tv_volume_group_name);
        holder.permissionText = (TextView) view.findViewById(R.id.tv_volume_group_permission);
        holder.relativeLayout = (RelativeLayout) view.findViewById(R.id.volume_group_rl);
        return holder;
    }

    @Override
    public void onBindViewHolder(VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerAdapterHolder holder, final int position) {
        holder.groupNameText.setText(groupList.get(position).getName());
        holder.permissionText.setText(groupList.get(position).getPrivilege() > 4 ? context.getString(R.string.volume_read_write_permission) : context.getString(R.string.volume_read_permission));
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumeGroupPermissionManagerInterface.onVolumeGroupClickListener(groupList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList == null ? 0 : groupList.size();
    }

    public void setVolumeGroupPermissionManagerInterfaceListener(VolumeGroupPermissionManagerInterface l) {
        this.volumeGroupPermissionManagerInterface = l;
    }

    public void setVolumeGroupPermissionList(List<Group> groupList) {
        this.groupList = groupList;
        notifyDataSetChanged();
    }

    public interface VolumeGroupPermissionManagerInterface {
        void onVolumeGroupClickListener(Group group);
    }

    public class VolumeGroupPermissionManagerAdapterHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout;
        TextView groupNameText;
        TextView permissionText;

        public VolumeGroupPermissionManagerAdapterHolder(View itemView) {
            super(itemView);
        }
    }
}
