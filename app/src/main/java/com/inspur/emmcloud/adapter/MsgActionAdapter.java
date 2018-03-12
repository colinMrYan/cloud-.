package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedActions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/2/9.
 */

public class MsgActionAdapter extends BaseAdapter {
    private Context context;
    private List<MsgContentExtendedActions.Action> actionList = new ArrayList<>();
    public MsgActionAdapter(Context context, List<MsgContentExtendedActions.Action> actionList) {
        this.context = context;
        this.actionList = actionList;
    }

    @Override
    public int getCount() {
        return actionList.size();
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
        TextView textView = new TextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
       // textView.setBackgroundResource(R.drawable.bg_corner);
        textView.setBackgroundResource(R.color.white);
        textView.setTextSize(15);
        textView.setGravity(Gravity.CENTER);
        textView.setText(actionList.get(position).getTitle());
        return textView;
    }
}
