package com.inspur.emmcloud.ui.chat.pop;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;

import java.util.ArrayList;
import java.util.List;

public class PopAdapter extends BaseAdapter {
    private List<String> dataList = new ArrayList<>();
    private Context context;

    public PopAdapter(Context context, List<String> dataList) {
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.pop_item_layout, null);
        }

        TextView textView = convertView.findViewById(R.id.pop_item_text);
        textView.setText(dataList.get(position));

        return convertView;
    }
}
