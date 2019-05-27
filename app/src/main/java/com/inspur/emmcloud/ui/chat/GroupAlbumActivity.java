package com.inspur.emmcloud.ui.chat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.GroupAlbumAdapter;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.common.GroupUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceRouterManager;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupAlbumActivity extends BaseActivity {

    public static final int GROUP_TYPE_MSG = 1;
    public static final int GROUP_TYPE_MESSAGE = 2;
//    private GridView albumGrid;
    @BindView(R.id.rl_no_channel_album)
    RelativeLayout noChannelAlbumLayout;
    @BindView(R.id.recycler_view_album)
    RecyclerView albumRecyclerView;
    private String cid;
    private ArrayList<String> imgUrlList = new ArrayList<>();
    private List<Msg> imgTypeMsgList;
    private List<Message> imgTypeMessageList;
    private Map<String, List<Message>> messageGroupByDayMap = new ArrayMap<String, List<Message>>();
    private Map<String, List<Msg>> msgGroupByDayMap = new ArrayMap<String, List<Msg>>();
    private GroupAlbumAdapter groupAlbumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_album;
    }

    private void init() {
        cid = getIntent().getExtras().getString("cid");
        getImgMsgList();
        noChannelAlbumLayout.setVisibility(imgUrlList.size() == 0 ? View.VISIBLE : View.GONE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GroupAlbumActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        albumRecyclerView.setLayoutManager(linearLayoutManager);
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            groupAlbumAdapter = new GroupAlbumAdapter(this, msgGroupByDayMap, GROUP_TYPE_MSG);
        } else {
            groupAlbumAdapter = new GroupAlbumAdapter(this, messageGroupByDayMap, GROUP_TYPE_MESSAGE);
        }
        groupAlbumAdapter.setOnGroupAlbumClickListener(new OnGroupAlbumClickListener() {
            @Override
            public void onGroupAlbumClick(View view, String imageUrl) {
                int position = imgUrlList.indexOf(imageUrl);
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.invalidate();
                int width = view.getWidth();
                int height = view.getHeight();
                Bundle bundle = new Bundle();
                bundle.putInt(ImagePagerV0Activity.PHOTO_SELECT_X_TAG, location[0]);
                bundle.putInt(ImagePagerV0Activity.PHOTO_SELECT_Y_TAG, location[1]);
                bundle.putInt(ImagePagerV0Activity.PHOTO_SELECT_W_TAG, width);
                bundle.putInt(ImagePagerV0Activity.PHOTO_SELECT_H_TAG, height);
                bundle.putInt(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
                bundle.putStringArrayList(ImagePagerActivity.EXTRA_IMAGE_URLS, imgUrlList);
                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                    bundle.putSerializable(ImagePagerV0Activity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMsgList);
                    bundle.putSerializable(ImagePagerV0Activity.EXTRA_CURRENT_IMAGE_MSG, imgTypeMsgList.get(position));
                    IntentUtils.startActivity(GroupAlbumActivity.this, ImagePagerV0Activity.class, bundle);
                } else {
                    bundle.putSerializable(ImagePagerV0Activity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMessageList);
                    bundle.putSerializable(ImagePagerV0Activity.EXTRA_CURRENT_IMAGE_MSG, imgTypeMessageList.get(position));
                    IntentUtils.startActivity(GroupAlbumActivity.this, ImagePagerActivity.class, bundle);
                }
            }
        });
        albumRecyclerView.setAdapter(groupAlbumAdapter);
    }

    private void getImgMsgList() {
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            imgTypeMsgList = MsgCacheUtil.getImgTypeMsgList(MyApplication.getInstance(), cid);
            for (Msg msg : imgTypeMsgList) {
                String url = APIUri.getPreviewUrl(msg.getImgTypeMsgImg());
                imgUrlList.add(url);
            }
            msgGroupByDayMap = GroupUtils.group(imgTypeMsgList, new ImageGroupByDate(GROUP_TYPE_MSG));
        } else {
            imgTypeMessageList = MessageCacheUtil.getImgTypeMessageList(MyApplication.getInstance(), cid);
            for (Message message : imgTypeMessageList) {
                String url = APIUri.getChatFileResouceUrl(message.getChannel(), message.getMsgContentMediaImage().getRawMedia());
                imgUrlList.add(url);
            }
            messageGroupByDayMap = GroupUtils.group(imgTypeMessageList, new ImageGroupByDate(GROUP_TYPE_MESSAGE));
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_header_choose:
                groupAlbumAdapter.setChangeSelectState();
                break;
        }
    }

    public interface OnGroupAlbumClickListener {
        void onGroupAlbumClick(View view, String imageUrl);
    }

    class ImageGroupByDate implements GroupUtils.GroupBy<String> {

        private int groupType = -1;

        public ImageGroupByDate(int groupType) {
            this.groupType = groupType;
        }

        @Override
        public String groupBy(Object obj) {
            String from = "";
            SimpleDateFormat format = new SimpleDateFormat(
                    getString(R.string.format_year_month));
            if (groupType == GROUP_TYPE_MSG) {
                Msg msg = (Msg) obj;
                from = msg.getTime() + "";
            } else if (groupType == GROUP_TYPE_MESSAGE) {
                Message message = (Message) obj;
                from = message.getCreationDate() + "";
            }
            if (!StringUtils.isBlank(from)) {
                Calendar calendarForm = TimeUtils.timeString2Calendar(from);
                return TimeUtils.calendar2FormatString(GroupAlbumActivity.this, calendarForm, format);
            }
            return "";
        }

    }
}
