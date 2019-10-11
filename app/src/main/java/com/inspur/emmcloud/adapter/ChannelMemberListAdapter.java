package com.inspur.emmcloud.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.PersonDto;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 成员列表适配器
 */
public class ChannelMemberListAdapter extends BaseAdapter implements SectionIndexer {
    private LayoutInflater inflater;
    private Activity mActivity;
    private List<PersonDto> personDtoList;
    private List<PersonDto> lastSelectUserList = new ArrayList<>();//上次选中的群成员list
    private List<PersonDto> selectedUserList = new ArrayList<>();//选中的群成员list

    public ChannelMemberListAdapter(Activity mActivity, List<PersonDto> sortDataList) {
        this.mActivity = mActivity;
        this.personDtoList = sortDataList;
    }

    public ChannelMemberListAdapter(Activity mActivity, List<PersonDto> sortDataList, List<PersonDto> lastSelectUserList) {
        this.mActivity = mActivity;
        this.personDtoList = sortDataList;
        this.selectedUserList = lastSelectUserList;
        this.lastSelectUserList = lastSelectUserList;
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param filterDateList
     */
    public void updateListView(List<PersonDto> filterDateList) {
        this.personDtoList = filterDateList;
        notifyDataSetChanged();
    }

    /**
     * 更新选中的list
     *
     * @param selectedUserList
     */
    public void updateSelectListViewData(List<PersonDto> selectedUserList) {
        this.selectedUserList = selectedUserList;
    }

    @Override
    public int getCount() {
        return personDtoList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogUtils.jasonDebug("position-==-" + position);
        ViewHolder holder = null;
        if (convertView == null) {
            inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.items_person_list, null);
            holder = new ViewHolder();
            holder.userHeadImg = (ImageView) convertView.findViewById(R.id.iv_user_head);
            holder.userNameTv = (TextView) convertView.findViewById(R.id.tv_user_name);
            holder.userDescTv = convertView.findViewById(R.id.tv_user_desc);
            holder.sideBarLetterTv = (TextView) convertView.findViewById(R.id.tv_member_slidebar);
//            holder.line = convertView.findViewById(R.id.v_line);
            holder.selectedImg = (ImageView) convertView.findViewById(R.id.img_member_selected);
            holder.contentRl = (RelativeLayout) convertView.findViewById(R.id.rl_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final PersonDto dto = personDtoList.get(position);
        if (dto != null) {
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (lastSelectUserList.size() == 0 && position == getPositionForSection(section)) {
                holder.sideBarLetterTv.setVisibility(View.VISIBLE);
                holder.sideBarLetterTv.setText("☆".equals(dto.getSortLetters()) ? dto.getSortLetters()
                        + mActivity.getString(R.string.administrators_of_channel) : dto.getSortLetters());
//                holder.line.setVisibility(View.GONE);
            } else {
                holder.sideBarLetterTv.setVisibility(View.GONE);
//                holder.line.setVisibility(View.GONE);
            }
            holder.userNameTv.setText(dto.getName());
            setUserDesc(dto, holder.userDescTv);
            String photoUrl;
            holder.userHeadImg.setTag("");
            if (dto.getUid().equals("10")) {
                holder.userHeadImg.setImageResource(R.drawable.ic_mention_all);
            } else {
                photoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), dto.getUid());
                ImageDisplayUtils.getInstance().displayImageByTag(holder.userHeadImg, photoUrl, R.drawable.icon_person_default);
            }
        }
        if (selectedUserList.contains(dto)) {
            holder.selectedImg.setVisibility(View.VISIBLE);
            holder.selectedImg.setImageResource(lastSelectUserList.contains(dto) ? R.drawable.icon_self_selected : R.drawable.icon_other_selected);
        } else {
            holder.selectedImg.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private void setUserDesc(PersonDto dto, TextView textView) {
        if (StringUtils.isBlank(dto.getUid())) {
            textView.setVisibility(View.GONE);
            return;
        }

        String enName = null;
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(dto.getUid());
        if (contactUser != null) {
            enName = contactUser.getNameGlobal();
        }

        if (StringUtils.isBlank(enName)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(enName);
            textView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return personDtoList.get(position).getSortLetters().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = personDtoList.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    class ViewHolder {
        ImageView userHeadImg;
        TextView sideBarLetterTv;
        TextView userNameTv;
        TextView userDescTv;
        //        View line;
        ImageView selectedImg;
        RelativeLayout contentRl;
    }

}
