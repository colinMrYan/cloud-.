package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MsgActionAdapter;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedActions;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.widget.NoScrollGridView;

import java.util.List;

/**
 * 展示活动卡片
 */
public class DisplayExtendedActionsMsg {

    /**
     * 富文本卡片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View getView(final Context context,
                               MsgRobot msg) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_extended_actions_view, null);
        ImageView posterImg = (ImageView)convertView.findViewById(R.id.poster_img);
        final MsgContentExtendedActions msgContentActions = msg.getMsgContentExtendedActions();
        RelativeLayout singleActionLayout = (RelativeLayout) convertView.findViewById(R.id.single_action_layout);
        NoScrollGridView actionGrid = (NoScrollGridView) convertView.findViewById(R.id.action_grid);
        TextView titleText = (TextView)convertView.findViewById(R.id.title_text);
        TextView descriptionText = (TextView)convertView.findViewById(R.id.description_text);
        MsgContentExtendedActions.Action singleAction = msgContentActions.getSingleAction();
        String poster = msgContentActions.getPoster();
        String title = msgContentActions.getTitle();
        String description = msgContentActions.getDescription();
        if (StringUtils.isBlank(poster)){
            posterImg.setVisibility(View.GONE);
        }else {
            ImageDisplayUtils.getInstance().displayImage(posterImg,poster, R.drawable.icon_photo_default);
        }
        if (StringUtils.isBlank(title)){
            titleText.setVisibility(View.GONE);
        }else {
            titleText.setText(msgContentActions.getTitle());
        }
        if (StringUtils.isBlank(description)){
            descriptionText.setVisibility(View.GONE);
        }else {
            descriptionText.setText(msgContentActions.getDescription());
        }
        if (singleAction != null) {
            actionGrid.setVisibility(View.GONE);
            singleActionLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = msgContentActions.getSingleAction().getUrl();                }
            });
        } else {
            singleActionLayout.setVisibility(View.GONE);
            final List<MsgContentExtendedActions.Action> actionList = msgContentActions.getActionList();
            if (msgContentActions.getArrangement().equals("horizontal")) {
                actionGrid.setNumColumns(actionList.size());
            } else {
                actionGrid.setNumColumns(1);
            }
            MsgActionAdapter msgActionAdapter = new MsgActionAdapter(context,actionList);
            actionGrid.setAdapter(msgActionAdapter);
            actionGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String url = actionList.get(position).getUrl();
                }
            });
        }


        return convertView;
    }

}
