package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yufuchang on 2018/08/17.
 */

public class VoiceCommunicationMemberAdapter extends RecyclerView.Adapter<VoiceCommunicationMemberAdapter.VoiceCommunicationHolder>{

    private Context context;
    private LayoutInflater inflater;
    private int index = 0;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
    public VoiceCommunicationMemberAdapter(Context context, List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList, int index){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.voiceCommunicationUserInfoBeanList = voiceCommunicationUserInfoBeanList;
        this.index = index;
    }

    @Override
    public VoiceCommunicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(index == 1){
            switch (voiceCommunicationUserInfoBeanList.size()){
                case 1:
                case 2:
                    view = inflater.inflate(R.layout.voice_communication_memeber_item,null);
                    break;
                case 3:
                    view = inflater.inflate(R.layout.voice_communication_memeber_item3,null);
                    break;
                case 4:
                    view = inflater.inflate(R.layout.voice_communication_memeber_item4,null);
                    break;
                case 5:
                    view = inflater.inflate(R.layout.voice_communication_memeber_item5,null);
                    break;
                default:
                    view = inflater.inflate(R.layout.voice_communication_memeber_item,null);
                    break;
            }
        }else if(index == 2){
            view = inflater.inflate(R.layout.voice_communication_memeber_item5,null);
        }else if(index == 3){
            view = inflater.inflate(R.layout.voice_communication_memeber_item_bottom,null);
        }else {
            view = inflater.inflate(R.layout.voice_communication_memeber_item,null);
        }
        VoiceCommunicationHolder holder = new VoiceCommunicationHolder(view);
        holder.headImg = (ImageView) view.findViewById(R.id.img_voice_communication_member_head);
        holder.nameText = (TextView) view.findViewById(R.id.tv_invite_voice_communication_member);
        holder.volumeImg = (ImageView) view.findViewById(R.id.img_voice_signal);
        holder.avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.avi_voice_communication);
        return holder;
    }

    @Override
    public void onBindViewHolder(VoiceCommunicationHolder holder, int position) {
        ImageDisplayUtils.getInstance().displayImage(holder.headImg,voiceCommunicationUserInfoBeanList.get(position).getHeadImageUrl(),R.drawable.icon_person_default);
        holder.nameText.setText(voiceCommunicationUserInfoBeanList.get(position).getUserName());
        int volume = voiceCommunicationUserInfoBeanList.get(position).getVolume();
        if(volume>0 && volume <= 85){
            holder.volumeImg.setVisibility(View.VISIBLE);
            holder.volumeImg.setImageResource(R.drawable.icon_signal_one);
        }else if(volume>0 && volume <= 170){
            holder.volumeImg.setVisibility(View.VISIBLE);
            holder.volumeImg.setImageResource(R.drawable.icon_signal_two);
        }else if(volume>0 && volume <= 255){
            holder.volumeImg.setVisibility(View.VISIBLE);
            holder.volumeImg.setImageResource(R.drawable.icon_signal_three);
        } else {
            holder.volumeImg.setVisibility(View.GONE);
        }
        //当通话人数为两个或者是邀请人的Adapter的时候不显示名字
        if((index == 1 && voiceCommunicationUserInfoBeanList.size() <= 2) || index == 3){
            holder.nameText.setVisibility(View.GONE);
        }
        if(voiceCommunicationUserInfoBeanList.get(position).getUserState() == 1 ||
                voiceCommunicationUserInfoBeanList.get(position).getUserId().
                        equals(MyApplication.getInstance().getUid()) || index == 3){
            holder.avLoadingIndicatorView.hide();
            holder.avLoadingIndicatorView.setVisibility(View.GONE);
        }else {
            holder.avLoadingIndicatorView.setVisibility(View.VISIBLE);
            holder.avLoadingIndicatorView.show();
        }
    }

    @Override
    public int getItemCount() {
        return voiceCommunicationUserInfoBeanList == null ? 0:voiceCommunicationUserInfoBeanList.size();
    }

    /**
     * 设置并刷新adapter
     * @param voiceCommunicationJoinChannelInfoBeanList
     * @param index
     */
    public void setMemberDataAndRefresh(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList,int index){
        this.voiceCommunicationUserInfoBeanList = voiceCommunicationJoinChannelInfoBeanList;
        this.index = index;
        notifyDataSetChanged();
    }

    public class VoiceCommunicationHolder extends RecyclerView.ViewHolder {
        ImageView headImg;
        TextView nameText;
        ImageView volumeImg;
        AVLoadingIndicatorView avLoadingIndicatorView;
        public VoiceCommunicationHolder(View itemView) {
            super(itemView);
        }
    }

}
