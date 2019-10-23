package com.inspur.emmcloud.ui.chat.emotion;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.inspur.emmcloud.R;

import java.util.List;

public class EmotionAdapter extends ArrayAdapter<String> {

    public EmotionAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.emotion_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.emotion_item_icon);
            convertView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        String filename = getItem(position);
        int resId = getContext().getResources().getIdentifier(filename, "drawable", getContext().getPackageName());
        holder.icon.setImageResource(resId);

        return convertView;
    }

    class ViewHolder {
        ImageView icon;
    }
}
