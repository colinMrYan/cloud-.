package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@ContentView(R.layout.activity_group_album)
public class GroupAlbumActivity extends BaseActivity {

    @ViewInject(R.id.gv_album)
    private GridView albumGrid;

    @ViewInject(R.id.rl_no_channel_album)
    private RelativeLayout noChannelAlbumLayout;

    private String cid;
    private ArrayList<String> imgUrlList = new ArrayList<String>();
    private List<Msg> imgTypeMsgList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        cid = getIntent().getExtras().getString("cid");
        getImgMsgList();
        noChannelAlbumLayout.setVisibility(imgUrlList.size() == 0 ? View.VISIBLE:View.GONE);
        albumGrid.setAdapter(new Adapter());
        albumGrid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.invalidate();
                int width = view.getWidth();
                int height = view.getHeight();
                Intent intent = new Intent(GroupAlbumActivity.this,
                        ImagePagerActivity.class);
                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMsgList);
                intent.putExtra(ImagePagerActivity.EXTRA_CURRENT_IMAGE_MSG, imgTypeMsgList.get(position));
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
                intent.putExtra("image_index", position);
                intent.putStringArrayListExtra("image_urls", imgUrlList);
                startActivity(intent);
            }
        });

    }

    /**
     * 获取图片消息列表
     */
    private void getImgMsgList() {
        // TODO Auto-generated method stub
        imgTypeMsgList = MsgCacheUtil.getImgTypeMsgList(GroupAlbumActivity.this, cid);
        for (Msg msg :imgTypeMsgList){
            String url = APIUri.getPreviewUrl(msg.getImgTypeMsgImg());
            imgUrlList.add(url);
        }
    }

    public void onClick(View v) {
        finish();
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return imgUrlList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater vi = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.group_album_item_view, null);
                holder.albumImg = (ImageView) convertView
                        .findViewById(R.id.album_img);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageDisplayUtils.getInstance().displayImage(holder.albumImg, imgUrlList.get(position), R.drawable.icon_photo_default);
            return convertView;
        }

    }

    private static class ViewHolder {
        ImageView albumImg;
    }
}
