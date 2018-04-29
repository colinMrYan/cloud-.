package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupAlbumActivity extends BaseActivity {

    private GridView albumGrid;
    private String cid;
    private ArrayList<String> imgUrlList = new ArrayList<String>();
    private List<Msg> imgTypeMsgList;
    private List<Message> imgTypeMessageList;
    private boolean isMessageV0 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_album);
        cid = getIntent().getExtras().getString("cid");
        getImgMsgList();
        albumGrid = (GridView) findViewById(R.id.album_grid);
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
                Bundle bundle = new Bundle();
                bundle.putInt(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
                bundle.putInt(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
                bundle.putInt(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
                bundle.putInt(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
                bundle.putInt("image_index", position);
                bundle.putStringArrayList("image_urls", imgUrlList);
                if (isMessageV0){
                    bundle.putSerializable(ImagePagerActivity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMsgList);
                    bundle.putSerializable(ImagePagerActivity.EXTRA_CURRENT_IMAGE_MSG, imgTypeMsgList.get(position));
                    IntentUtils.startActivity(GroupAlbumActivity.this,ImagePagerActivity.class,bundle);
                }else {
                    bundle.putSerializable(ImagePagerActivity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMessageList);
                    bundle.putSerializable(ImagePagerActivity.EXTRA_CURRENT_IMAGE_MSG, imgTypeMessageList.get(position));
                    IntentUtils.startActivity(GroupAlbumActivity.this,ChatImagePagerActivity.class,bundle);
                }

            }
        });

    }

    /**
     * 获取图片消息列表
     */
    private void getImgMsgList() {
        // TODO Auto-generated method stub
        if (isMessageV0){
            imgTypeMsgList = MsgCacheUtil.getImgTypeMsgList(MyApplication.getInstance(), cid);
            for (Msg msg :imgTypeMsgList){
                String url = APIUri.getPreviewUrl(msg.getImgTypeMsgImg());
                imgUrlList.add(url);
            }

        }else {
            imgTypeMessageList = MessageCacheUtil.getImgTypeMessageList(MyApplication.getInstance(), cid);
            for (Message message:imgTypeMessageList) {
                String url = APIUri.getPreviewUrl(message.getMsgContentMediaImage().getRawMedia());
                imgUrlList.add(url);
            }

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
