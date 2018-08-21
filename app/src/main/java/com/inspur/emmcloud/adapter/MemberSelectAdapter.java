package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.ChannelSelectVoiceVideoMembersActivity;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/20.
 */

public class MemberSelectAdapter extends RecyclerView.Adapter<MemberSelectAdapter.MemberSelectHold>{

    private Context context;
    private LayoutInflater inflater;
    private List<ContactUser> contactUserList;
    private List<ContactUser> selectedUserList = new ArrayList<>();
    private List<ContactUser> lastSelectUserList = new ArrayList<>();
    private ChannelSelectVoiceVideoMembersActivity.OnMemeberSelectedListener listener;
    public MemberSelectAdapter(Context context, List<ContactUser> contactUserList,List<ContactUser> lastSelectUserList){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.contactUserList = contactUserList;
        this.lastSelectUserList = lastSelectUserList;
        this.lastSelectUserList.add(ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid()));
        selectedUserList.addAll(lastSelectUserList);
    }

    @Override
    public MemberSelectHold onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.member_select_item_view,null);
        MemberSelectHold holder = new MemberSelectHold(view);
        holder.linearLayoutMemberItem = (LinearLayout) view.findViewById(R.id.ll_member_select_item);
        holder.imgUserHead = (ImageView) view.findViewById(R.id.img_user_head);
        holder.imgSelected = (ImageView) view.findViewById(R.id.img_member_selected);
        holder.tvUserName = (TextView) view.findViewById(R.id.tv_user_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MemberSelectHold holder, final int position) {
        final ContactUser contactUser = contactUserList.get(position);
        ImageDisplayUtils.getInstance().displayImage(holder.imgUserHead,
                APIUri.getChannelImgUrl(MyApplication.getInstance(),
                        contactUser.getId()),R.drawable.icon_person_default);
        holder.tvUserName.setText(contactUserList.get(position).getName());
        if(!lastSelectUserList.contains(contactUser)){
            holder.linearLayoutMemberItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedUserList.size() < 9 || selectedUserList.contains(contactUser)){
                        if(!holder.linearLayoutMemberItem.isSelected()){
                            holder.linearLayoutMemberItem.setSelected(true);
                            holder.imgSelected.setVisibility(View.VISIBLE);
                            holder.imgSelected.setImageResource(R.drawable.icon_other_selected);
                            selectedUserList.add(contactUser);
                        }else {
                            holder.linearLayoutMemberItem.setSelected(false);
                            holder.imgSelected.setVisibility(View.GONE);
                            selectedUserList.remove(contactUser);
                        }
                        listener.onMemberSelected(selectedUserList);
                    }else {
                        ToastUtils.show(context,"云+目前支持九人语音");
                    }
                }
            });
        }
        if(selectedUserList.contains(contactUser)){
            holder.imgSelected.setVisibility(View.VISIBLE);
            holder.imgSelected.setImageResource(lastSelectUserList.contains(contactUser)?R.drawable.icon_self_selected:R.drawable.icon_other_selected);
        }else {
            holder.imgSelected.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return contactUserList.size();
    }

    /**
     * 选人回调接口
     * @param l
     */
    public void setMemberSelectedInterface(ChannelSelectVoiceVideoMembersActivity.OnMemeberSelectedListener l){
        this.listener = l;
    }

    class MemberSelectHold extends RecyclerView.ViewHolder{
        private LinearLayout linearLayoutMemberItem;
        private ImageView imgUserHead;
        private TextView tvUserName;
        private ImageView imgSelected;
        public MemberSelectHold(View itemView) {
            super(itemView);
        }
    }
}
