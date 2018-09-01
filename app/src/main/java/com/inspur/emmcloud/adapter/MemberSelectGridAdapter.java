package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

import java.util.List;

/**
 * Created by yufuchang on 2018/8/21.
 */

public class MemberSelectGridAdapter extends RecyclerView.Adapter<MemberSelectGridAdapter.MemberSelectedGridHold>{

    private Context context;
    private List<ContactUser> contactUserList;
    private LayoutInflater inflater;
    public MemberSelectGridAdapter(Context context,List<ContactUser> contactUserList){
        this.context = context;
        this.contactUserList = contactUserList;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public MemberSelectedGridHold onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.voice_communication_memeber_item,null);
        MemberSelectedGridHold holder = new MemberSelectedGridHold(view);
        holder.userHeadImg = (ImageView) view.findViewById(R.id.img_voice_communication_member_head);
        return holder;
    }

    @Override
    public void onBindViewHolder(MemberSelectedGridHold holder, int position) {
        setImgSize(holder.userHeadImg);
        ImageDisplayUtils.getInstance().displayImage(holder.userHeadImg, APIUri.getChannelImgUrl(MyApplication.getInstance(),
                contactUserList.get(position).getId()),R.drawable.icon_person_default);
    }

    /**
     * 设置头像padding
     * @param headImg
     */
    private void setImgSize(ImageView headImg) {
        int size = DensityUtil.dip2px(context,55);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size,
                size);//两个400分别为添加图片的大小
        headImg.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return contactUserList.size();
    }

    /**
     * 设置并刷新数据
     * @param contactUserList
     */
    public void setAndRefreshSelectMemberData(List<ContactUser> contactUserList){
        this.contactUserList = contactUserList;
        notifyDataSetChanged();
    }

    class MemberSelectedGridHold extends RecyclerView.ViewHolder{
        private ImageView userHeadImg;
        public MemberSelectedGridHold(View itemView) {
            super(itemView);
        }
    }
}
