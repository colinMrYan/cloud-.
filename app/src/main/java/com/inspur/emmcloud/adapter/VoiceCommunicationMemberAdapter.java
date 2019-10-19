package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yufuchang on 2018/08/17.
 */

public class VoiceCommunicationMemberAdapter extends RecyclerView.Adapter<VoiceCommunicationMemberAdapter.VoiceCommunicationHolder> {

    private Context context;
    private LayoutInflater inflater;
    private int index = 0;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();

    public VoiceCommunicationMemberAdapter(Context context, List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList, int index) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.voiceCommunicationUserInfoBeanList = voiceCommunicationUserInfoBeanList;
        this.index = index;
    }

    @Override
    public VoiceCommunicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.voice_communication_memeber_item, null);
        VoiceCommunicationHolder holder = new VoiceCommunicationHolder(view);
        holder.headImg = (ImageView) view.findViewById(R.id.img_voice_communication_member_head);
        holder.nameTv = (TextView) view.findViewById(R.id.tv_invite_voice_communication_member);
        holder.volumeImg = (ImageView) view.findViewById(R.id.img_voice_signal);
        holder.avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.avi_voice_communication);
        return holder;
    }

    @Override
    public void onBindViewHolder(VoiceCommunicationHolder holder, int position) {
        setUserHeadImgSize(holder.headImg, index);
        //头像源数据修改为本地，注释掉的是从接口中读取的url
        ImageDisplayUtils.getInstance().displayImage(holder.headImg, voiceCommunicationUserInfoBeanList.get(position).getHeadImageUrl(), R.drawable.icon_person_default);
//        ImageDisplayUtils.getInstance().displayImage(holder.headImg, APIUri.getUserIconUrl(MyApplication.getInstance(), voiceCommunicationUserInfoBeanList.get(position).getUserId()), R.drawable.icon_person_default);
        holder.nameTv.setText(voiceCommunicationUserInfoBeanList.get(position).getUserName());
        //音量控制逻辑
        int volume = voiceCommunicationUserInfoBeanList.get(position).getVolume();
        int volumeLevel = (int) Math.rint(volume / 85);
        switch (volumeLevel) {
            case 0:
                holder.volumeImg.setVisibility(View.GONE);
                break;
            case 1:
                holder.volumeImg.setVisibility(View.VISIBLE);
                holder.volumeImg.setImageResource(R.drawable.icon_signal_one);
                break;
            case 2:
                holder.volumeImg.setVisibility(View.VISIBLE);
                holder.volumeImg.setImageResource(R.drawable.icon_signal_two);
                break;
            case 3:
                holder.volumeImg.setVisibility(View.VISIBLE);
                holder.volumeImg.setImageResource(R.drawable.icon_signal_three);
                break;
            default:
                holder.volumeImg.setVisibility(View.GONE);
                break;
        }
        //当通话人数为两个或者是邀请人的Adapter的时候不显示名字
        holder.nameTv.setVisibility(((index == 1 && voiceCommunicationUserInfoBeanList.size() <= 2) || index == 3) ? View.GONE : View.VISIBLE);
        if (voiceCommunicationUserInfoBeanList.get(position).getConnectState() != 0 ||
                voiceCommunicationUserInfoBeanList.get(position).getUserId().
                        equals(MyApplication.getInstance().getUid()) || index == 3) {
            holder.avLoadingIndicatorView.hide();
            holder.avLoadingIndicatorView.setVisibility(View.GONE);
        } else {
            holder.avLoadingIndicatorView.setVisibility(View.VISIBLE);
            holder.avLoadingIndicatorView.show();
        }
    }

    /**
     * 设置头像大小，不同个数时大小有所不同,默认情况不改变size
     *
     * @param index
     */
    private void setUserHeadImgSize(ImageView headImg, int index) {
        switch (index) {
            case 1:
                switch (voiceCommunicationUserInfoBeanList.size()) {
                    case 3:
                        setImgSize(headImg, 1);
                        break;
                    case 4:
                        setImgSize(headImg, 2);
                        break;
                    case 5:
                        setImgSize(headImg, 3);
                        break;
                }
                break;
            case 2:
                setImgSize(headImg, 2);
                break;
            case 3:
                setImgSize(headImg, 3);
                break;
        }
    }

    /**
     * 设置头像padding
     *
     * @param headImg
     * @param i
     */
    private void setImgSize(ImageView headImg, int i) {
        int size = DensityUtil.dip2px(context, 70 - 5 * i);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size,
                size);//两个400分别为添加图片的大小
        headImg.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return voiceCommunicationUserInfoBeanList == null ? 0 : voiceCommunicationUserInfoBeanList.size();
    }

    /**
     * 设置并刷新adapter
     *
     * @param voiceCommunicationJoinChannelInfoBeanList
     * @param index
     */
    public void setMemberDataAndRefresh(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList, int index) {
        this.voiceCommunicationUserInfoBeanList = voiceCommunicationJoinChannelInfoBeanList;
        this.index = index;
        notifyDataSetChanged();
    }

    public class VoiceCommunicationHolder extends RecyclerView.ViewHolder {
        ImageView headImg;
        TextView nameTv;
        ImageView volumeImg;
        AVLoadingIndicatorView avLoadingIndicatorView;

        public VoiceCommunicationHolder(View itemView) {
            super(itemView);
        }
    }

}
