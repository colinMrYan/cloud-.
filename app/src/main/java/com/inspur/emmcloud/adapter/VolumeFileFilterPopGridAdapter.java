package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

/**
 * Created by chenmch on 2017/11/17.
 */

public class VolumeFileFilterPopGridAdapter extends BaseAdapter{
    private Context context;
    private String[] filterTypeNames = {"文档","图片","应用","视频","其他"};
    private int[] filterTypeIconIds = {R.drawable.ic_volume_file_typ_document,R.drawable.ic_volume_file_typ_img,R.drawable.ic_volume_file_typ_app,R.drawable.ic_volume_file_typ_video,R.drawable.ic_volume_file_typ_other};
    public VolumeFileFilterPopGridAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.app_volume_file_filter_pop_grid_item_view,null);
        ((ImageView)convertView.findViewById(R.id.filter_type_img)).setImageResource(filterTypeIconIds[position]);
        ((TextView)convertView.findViewById(R.id.filter_type_text)).setText(filterTypeNames[position]);
        return convertView;
    }
}
