package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

import java.util.List;

/**
 * Created by yufuchang on 2019/1/21.
 */

public class GroupAlbumItemAdapter extends BaseAdapter {

    private List<String> imgUrlList;
    private Context context;
    public GroupAlbumItemAdapter(Context context, List<String> imgUrlList){
        this.imgUrlList = imgUrlList;
        this.context = context;
    }

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
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.group_album_item_view, null);
            holder.albumImg =  convertView
                    .findViewById(R.id.album_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageDisplayUtils.getInstance().displayImage(holder.albumImg, imgUrlList.get(position), R.drawable.default_image);
        return convertView;
    }


    private class ViewHolder {
        ImageView albumImg;
    }

}


