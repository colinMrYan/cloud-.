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

public class VolumeFileFilterPopGridAdapter extends BaseAdapter {
    private Context context;
    private int[] filterTypeNameIds = {R.string.docunment, R.string.picture, R.string.audio, R.string.video, R.string.other};
    private int[] filterTypeIconIds = {R.drawable.baselib_file_type_document, R.drawable.baselib_file_type_img,
            R.drawable.baselib_file_type_audio, R.drawable.baselib_file_type_video, R.drawable.baselib_file_type_more};

    public VolumeFileFilterPopGridAdapter(Context context) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.file_filter_pop_grid_item_view, null);
        ((ImageView) convertView.findViewById(R.id.filter_type_img)).setImageResource(filterTypeIconIds[position]);
        ((TextView) convertView.findViewById(R.id.filter_type_text)).setText(filterTypeNameIds[position]);
        return convertView;
    }
}
