package com.inspur.emmcloud.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.PersonDto;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.RobotCacheUtils;
import com.inspur.emmcloud.util.UriUtils;

import java.util.List;


/**
 * 成员列表适配器
 */
public class ChannelMemberListAdapter extends BaseAdapter implements SectionIndexer {

    private LayoutInflater inflater;

    private Activity mActivity;

    private List<PersonDto> list;


    public ChannelMemberListAdapter(Activity mActivity, List<PersonDto> sortDataList) {
        this.mActivity = mActivity;
        this.list = sortDataList;
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param filterDateList
     */
    public void updateListView(List<PersonDto> filterDateList) {
        this.list = filterDateList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
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
        ViewHolder holder = null;
        if (convertView == null) {
            inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.items_person_list, null);
            holder = new ViewHolder();
            holder.headimg = (ImageView) convertView.findViewById(R.id.head);
            holder.username = (TextView) convertView.findViewById(R.id.title);
            holder.slidebarleter = (TextView) convertView.findViewById(R.id.catalog);
            holder.line = (TextView) convertView.findViewById(R.id.line);
            holder.contentlayout = (LinearLayout) convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final PersonDto dto = list.get(position);

        if (dto != null) {
            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                holder.slidebarleter.setVisibility(View.VISIBLE);
                holder.slidebarleter.setText("☆".equals(dto.getSortLetters()) ? dto.getSortLetters()
                        + mActivity.getString(R.string.administrators_of_channel) : dto.getSortLetters());
                holder.line.setVisibility(View.VISIBLE);
            } else {
                holder.slidebarleter.setVisibility(View.GONE);
                holder.line.setVisibility(View.GONE);
            }
            holder.username.setText(dto.getName());
            String photoUrl = "";
            if (dto.getUtype().equals("robot")) {
                photoUrl = UriUtils.getRobotIconUri(RobotCacheUtils
                        .getRobotById(mActivity, dto.getUid())
                        .getAvatar());
            } else {
                photoUrl = UriUtils.getChannelImgUri(mActivity, dto.getUid());
            }
            ImageDisplayUtils.getInstance().displayImage(holder.headimg, photoUrl, R.drawable.icon_person_default);


        }
        return convertView;
    }

    class ViewHolder {
        ImageView headimg;
        TextView slidebarleter;
        TextView username;
        TextView line;
        LinearLayout contentlayout;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    public int getSectionForPosition(int position) {
        return list.get(position).getSortLetters().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getSortLetters();
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

}
