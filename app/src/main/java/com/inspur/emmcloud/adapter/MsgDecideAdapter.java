package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Option;

import java.util.ArrayList;
import java.util.List;


public class MsgDecideAdapter extends BaseAdapter {
    private Context context;
    private List<Option> actionList = new ArrayList<>();
    private String arrangement;

    public MsgDecideAdapter(Context context, List<Option> actionList, String arrangement) {
        this.context = context;
        this.actionList = actionList;
        this.arrangement = arrangement;
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
        convertView = LayoutInflater.from(context).inflate((arrangement.equals("horizontal") ?
                R.layout.chat_msg_card_action_horizontal_item_view : R.layout.chat_msg_card_action_vertical_item_view), null);
        TextView actionText = convertView.findViewById(R.id.action_text);
        View dividerView = convertView.findViewById(R.id.divider_view);
        actionText.setText(actionList.get(position).getTitle());
        dividerView.setVisibility((position == getCount() - 1) ? View.GONE : View.VISIBLE);
        return convertView;
    }
}
