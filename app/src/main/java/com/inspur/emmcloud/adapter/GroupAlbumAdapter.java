package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.ui.chat.GroupAlbumActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yufuchang on 2019/1/21.
 */

public class GroupAlbumAdapter extends RecyclerView.Adapter<GroupAlbumAdapter.AlbumViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private Map<String, List<Message>> messageGroupByDayMap = new HashMap<>();
    private Map<String, List<Msg>> msgGroupByDayMap = new HashMap<>();
    private ArrayList<String> arrayList = null;
    private GroupAlbumActivity.OnGroupAlbumClickListener listener;
    private boolean isSelectState = false;

    public GroupAlbumAdapter(Context context, Object map, int type) {
        this.context = context;
        Set<String> keySet = new HashSet<>();
        if (type == GroupAlbumActivity.GROUP_TYPE_MESSAGE && map != null) {
            this.messageGroupByDayMap = ((Map<String, List<Message>>) map);
            keySet = ((Map<String, List<Message>>) map).keySet();
        } else if (type == GroupAlbumActivity.GROUP_TYPE_MSG && map != null) {
            this.msgGroupByDayMap = ((Map<String, List<Msg>>) map);
            keySet = ((Map<String, List<Msg>>) map).keySet();
        }
        init(keySet);
    }

    public void setOnGroupAlbumClickListener(GroupAlbumActivity.OnGroupAlbumClickListener listener) {
        this.listener = listener;
    }

    public void setChangeSelectState() {
        this.isSelectState = isSelectState ? false : true;
        notifyDataSetChanged();
    }

    private void init(Set<String> keySet) {
        arrayList = new ArrayList<>(keySet);
        Collections.sort(arrayList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int i = lhs.compareTo(rhs);
                if (i > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_group_album_item, null);
        AlbumViewHolder holder = new AlbumViewHolder(view);
        holder.imageGroupTitleText = view.findViewById(R.id.tv_image_group_title);
        holder.imageGroupGridView = view.findViewById(R.id.gv_album_item);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        String itemName = arrayList.get(position);
        SimpleDateFormat format = new SimpleDateFormat(
                context.getString(R.string.format_year_month));
        Calendar calendar = TimeUtils.timeString2Calendar(System.currentTimeMillis() + "");
        String yearAndMonth = TimeUtils.calendar2FormatString(context, calendar, format);
        holder.imageGroupTitleText.setText(yearAndMonth.equals(itemName) ? context.getString(R.string.current_month) : itemName);
        final ArrayList<String> imgUrlList = new ArrayList<>();
        if (messageGroupByDayMap.size() > 0) {
            for (Message message : messageGroupByDayMap.get(itemName)) {
                String url = APIUri.getChatFileResouceUrl(message.getChannel(), message.getMsgContentMediaImage().getRawMedia());
                MsgContentMediaImage msgContentMediaImage = message.getMsgContentMediaImage();
                if (message.getMsgContentMediaImage().getPreviewHeight() != 0
                        && message.getMsgContentMediaImage().getPreviewHeight() != message.getMsgContentMediaImage().getRawHeight()
                        && message.getMsgContentMediaImage().getPreviewWidth() != message.getMsgContentMediaImage().getRawWidth()) {
                    url = url + "&resize=true&w=" + msgContentMediaImage.getPreviewWidth() + "&h=" + msgContentMediaImage.getPreviewHeight();
                }
                imgUrlList.add(url);
            }
        } else if (msgGroupByDayMap.size() > 0) {
            for (Msg msg : msgGroupByDayMap.get(itemName)) {
                String url = APIUri.getPreviewUrl(msg.getImgTypeMsgImg());
                imgUrlList.add(url);
            }
        }
        GroupAlbumItemAdapter groupAlbumItemAdapter = new GroupAlbumItemAdapter(context, imgUrlList);
        groupAlbumItemAdapter.setShowSelectState(isSelectState);
        Configuration configuration = context.getResources().getConfiguration();
        // 适配横屏图片显示
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            holder.imageGroupGridView.setNumColumns(6);

        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            holder.imageGroupGridView.setNumColumns(4);

        }
        holder.imageGroupGridView.setAdapter(groupAlbumItemAdapter);
        holder.imageGroupGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onGroupAlbumClick(view, imgUrlList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (messageGroupByDayMap != null && messageGroupByDayMap.size() > 0) {
            return messageGroupByDayMap.size();
        } else if (msgGroupByDayMap != null && msgGroupByDayMap.size() > 0) {
            return msgGroupByDayMap.size();
        }
        return 0;
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView imageGroupTitleText;
        GridView imageGroupGridView;

        public AlbumViewHolder(View itemView) {
            super(itemView);
        }
    }
}
