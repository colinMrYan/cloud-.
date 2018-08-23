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
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
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
        holder.avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.avi_voice_communication);
        return holder;
    }

    @Override
    public void onBindViewHolder(VoiceCommunicationHolder holder, int position) {
        String url = APIUri.getUserIconUrl(context, voiceCommunicationUserInfoBeanList.get(position).getUserId());
        ImageDisplayUtils.getInstance().displayImage(holder.headImg,url,R.drawable.icon_person_default);
        holder.nameText.setText(ContactUserCacheUtils.getContactUserByUid(voiceCommunicationUserInfoBeanList.get(position).getUserId()).getName());
        if(holder.avLoadingIndicatorView != null){
            holder.avLoadingIndicatorView.setVisibility(View.GONE);
//            if(position %2 == 0){
//                holder.avLoadingIndicatorView.show();
//            }else{
//                holder.avLoadingIndicatorView.hide();
//            }
        }
    }

    @Override
    public int getItemCount() {
        return voiceCommunicationUserInfoBeanList.size();
    }

    public class VoiceCommunicationHolder extends RecyclerView.ViewHolder {
        ImageView headImg;
        TextView nameText;
        AVLoadingIndicatorView avLoadingIndicatorView;
        public VoiceCommunicationHolder(View itemView) {
            super(itemView);
        }
    }

}
