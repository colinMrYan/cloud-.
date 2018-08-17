package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yufuchang on 2018/08/17.
 */

public class VoiceCommunicationMemberAdapter extends RecyclerView.Adapter<VoiceCommunicationMemberAdapter.VoiceCommunicationHolder>{

    private Context context;
    private LayoutInflater inflater;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList = new ArrayList<>();
    public VoiceCommunicationMemberAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public VoiceCommunicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (voiceCommunicationJoinChannelInfoBeanList.size()){
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
//        View view  = inflater.inflate(R.layout.voice_communication_memeber_item,null);
        VoiceCommunicationHolder holder = new VoiceCommunicationHolder(view);
        holder.headImg = (ImageView) view.findViewById(R.id.img_voice_communication_member_head);
        holder.nameText = (TextView) view.findViewById(R.id.tv_invite_voice_communication_member);
        return holder;
    }

    @Override
    public void onBindViewHolder(VoiceCommunicationHolder holder, int position) {
        LogUtils.YfcDebug("绑定Holder");
        String url = APIUri.getUserIconUrl(context,voiceCommunicationJoinChannelInfoBeanList.get(position).getUserId());
        ImageDisplayUtils.getInstance().displayImage(holder.headImg,url,R.drawable.icon_person_default);
        holder.nameText.setText(ContactUserCacheUtils.getContactUserByUid(voiceCommunicationJoinChannelInfoBeanList.get(position).getUserId()).getName());
//        if(voiceCommunicationJoinChannelInfoBeanList.size() > 2){
//            int padding = DensityUtil.dip2px(context,(voiceCommunicationJoinChannelInfoBeanList.size() - 2)*10);
//            LogUtils.YfcDebug("设置Image的大小"+padding);
//            ViewGroup.LayoutParams params = holder.headImg.getLayoutParams();
//            params.width = DensityUtil.dip2px(context,(70-(voiceCommunicationJoinChannelInfoBeanList.size() - 2)*5));
//            params.height = DensityUtil.dip2px(context,(70-(voiceCommunicationJoinChannelInfoBeanList.size() - 2)*5));
//            holder.headImg.setLayoutParams(params);
//            holder.headImg.setPadding(padding,padding,padding,padding);
//        }
    }

    @Override
    public int getItemCount() {
        return voiceCommunicationJoinChannelInfoBeanList.size();
    }

    public class VoiceCommunicationHolder extends RecyclerView.ViewHolder {
        ImageView headImg;
        TextView nameText;
        public VoiceCommunicationHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 设置并刷新数据
     * @param voiceCommunicationJoinChannelInfoBeanList
     */
    public void setAndRefreshVoiceCommunicationAdapterData(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList){
        this.voiceCommunicationJoinChannelInfoBeanList = voiceCommunicationJoinChannelInfoBeanList;
        notifyDataSetChanged();
    }

}
