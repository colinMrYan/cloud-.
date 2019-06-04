package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.widget.CircleTextImageView;

public class ChannelMemActivity extends BaseActivity {

    private String[] memberArray;
    @Override
    public void onCreate() {
        String title = getIntent().getStringExtra("title");
        ((TextView) findViewById(R.id.header_text)).setText(title);
        memberArray = getIntent().getStringArrayExtra("members");
        if (memberArray != null && memberArray.length > 0) {
            GridView memberGrid = (GridView) findViewById(R.id.gv_member);
            memberGrid.setAdapter(new Adapter());
            memberGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.putExtra("uid", memberArray[position]);
                    intent.setClass(getApplicationContext(),
                            UserInfoActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_channel_mem;
    }

    public void onClick(View v) {
        finish();
    }

    public static class ViewHolder {
        CircleTextImageView memberHeadImg;
        TextView nameText;
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return memberArray.length;
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
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.channel_member_item_view,
                        null);
                viewHolder.memberHeadImg = (CircleTextImageView) convertView
                        .findViewById(R.id.member_head_img);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.tv_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String uid = memberArray[position];
            ImageDisplayUtils.getInstance().displayImage(viewHolder.memberHeadImg,
                    APIUri.getUserIconUrl(ChannelMemActivity.this, uid), R.drawable.icon_photo_default);
            viewHolder.nameText.setText("");
            return convertView;
        }
    }

}
