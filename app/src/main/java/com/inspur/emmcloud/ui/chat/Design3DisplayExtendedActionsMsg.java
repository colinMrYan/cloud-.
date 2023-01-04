package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MsgActionAdapter;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.chat.Action;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedActions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * 展示活动卡片
 */
public class Design3DisplayExtendedActionsMsg {

    private static Design3DisplayExtendedActionsMsg mInstance;
    private Context context;
    private LoadingDialog loadingDlg;

    private Design3DisplayExtendedActionsMsg(Context context) {
        this.context = context;
        loadingDlg = new LoadingDialog(context);
    }

    public static Design3DisplayExtendedActionsMsg getInstance(Context context) {
        if (mInstance == null) {
            synchronized (Design3DisplayExtendedActionsMsg.class) {
                if (mInstance == null) {
                    mInstance = new Design3DisplayExtendedActionsMsg(context);
                }
            }
        }
        return mInstance;
    }

    public View getView(Message msg) {
        View convertView = LayoutInflater.from(context).inflate(
                R.layout.design3_chat_msg_card_child_extended_actions_view, null);
        final boolean isMyMsg = msg.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = (BubbleLayout) convertView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        cardLayout.setStrokeWidth(0);
        ImageView posterImg = (ImageView) convertView.findViewById(R.id.poster_img);
        final MsgContentExtendedActions msgContentActions = msg.getMsgContentExtendedActions();
        RelativeLayout singleActionLayout = (RelativeLayout) convertView.findViewById(R.id.single_action_layout);
        NoScrollGridView actionGrid = (NoScrollGridView) convertView.findViewById(R.id.action_grid);
        TextView titleText = (TextView) convertView.findViewById(R.id.tv_name_tips);
        TextView descriptionText = (TextView) convertView.findViewById(R.id.description_text);
        Action singleAction = msgContentActions.getSingleAction();
        String poster = msgContentActions.getPoster();
        String title = msgContentActions.getTitle();
        String description = msgContentActions.getDescription();
        if (StringUtils.isBlank(poster)) {
            posterImg.setVisibility(View.GONE);
        } else {
            ImageDisplayUtils.getInstance().displayImage(posterImg, poster, R.drawable.icon_photo_default);
        }
        if (StringUtils.isBlank(title)) {
            titleText.setVisibility(View.GONE);
        } else {
            titleText.setText(msgContentActions.getTitle());
        }
        if (StringUtils.isBlank(description)) {
            descriptionText.setVisibility(View.GONE);
        } else {
            descriptionText.setText(msgContentActions.getDescription());
        }
        if (singleAction != null) {
            actionGrid.setVisibility(View.GONE);
            singleActionLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAction(context, msgContentActions.getSingleAction().getTitle());
                }
            });
        } else {
            singleActionLayout.setVisibility(View.GONE);
            final List<Action> actionList = msgContentActions.getActionList();
            if (msgContentActions.getArrangement().equals("horizontal")) {
                actionGrid.setNumColumns(actionList.size());
            } else {
                actionGrid.setNumColumns(1);
            }
            MsgActionAdapter msgActionAdapter = new MsgActionAdapter(context, actionList, msgContentActions.getArrangement());
            actionGrid.setAdapter(msgActionAdapter);
            actionGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openAction(context, actionList.get(position).getTitle());
                }
            });
        }


        return convertView;
    }


    private void openAction(Context context, String actionContent) {
        if (!StringUtils.isBlank(actionContent) && NetUtils.isNetworkConnected(context)) {
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SEND_ACTION_CONTENT_MESSAGE, actionContent));
        }

    }

}
