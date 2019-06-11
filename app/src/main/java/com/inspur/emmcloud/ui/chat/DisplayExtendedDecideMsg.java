package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MsgDecideAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedDecide;
import com.inspur.emmcloud.bean.chat.Option;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;
import com.inspur.imp.api.ImpActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class DisplayExtendedDecideMsg {

    private static final String DECIDE_HORIZONTAL = "horizontal";
    private static final String DECIDE_OPEN_URL = "open-url";
    private static final String DECIDE_REPLY = "reply";
    private static final String DECIDE_BOT_ACTION = "bot-action";
    private static final String DECIDE_ECC = "ecc-";
    private static final String DECIDE_HTTP = "http";
    private static final String DECIDE_URI = "uri";
    /**
     * 决策卡片
     * @param msg
     */
    public static View getView(Message msg, final Context context) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.chat_msg_card_child_extended_decide_view, null);
        boolean isMyMsg = msg.getFromUser().equals(MyApplication.getInstance().getUid());
        BubbleLayout cardLayout = convertView.findViewById(R.id.bl_card);
        cardLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        ImageView posterImg = convertView.findViewById(R.id.iv_post_image);
        final MsgContentExtendedDecide msgContentDecide = msg.getMsgContentExtendedDecide();
        NoScrollGridView optionGrid = convertView.findViewById(R.id.gv_options_grid);
        TextView titleText = convertView.findViewById(R.id.tv_name_tips);
        TextView descriptionText = convertView.findViewById(R.id.tv_description);
        String poster = msgContentDecide.getPoster();
        String title = msgContentDecide.getTitle();
        String description = msgContentDecide.getDescription();
        //设置决策卡片封面
        if (StringUtils.isBlank(poster)) {
            posterImg.setVisibility(View.GONE);
        } else {
            ImageDisplayUtils.getInstance().displayImage(posterImg, poster, R.drawable.icon_photo_default);
        }
        //设置决策卡片标题
        if (StringUtils.isBlank(title)) {
            titleText.setVisibility(View.GONE);
        } else {
            titleText.setText(title);
        }
        //设置决策卡片描述
        if (StringUtils.isBlank(description)) {
            descriptionText.setVisibility(View.GONE);
        } else {
            descriptionText.setText(description);
        }
        //决策卡片选项
        final List<Option> optionList = msgContentDecide.getOptionList();
        if (msgContentDecide.getArrangement().equals(DECIDE_HORIZONTAL)) {
            optionGrid.setNumColumns(optionList.size());
        } else {
            optionGrid.setNumColumns(1);
        }
        MsgDecideAdapter msgDecideAdapter = new MsgDecideAdapter(context, optionList, msgContentDecide.getArrangement());
        optionGrid.setAdapter(msgDecideAdapter);
        optionGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!msgContentDecide.isLockAfterSelection()){
                    LogUtils.YfcDebug("11111111111111");
                    openOption(context, optionList.get(position));
                }else{
                    LogUtils.YfcDebug("222222222222222");
                    if(msgContentDecide.isCanClickAgain()){
                        openOption(context,optionList.get(position));
                        msgContentDecide.setCanClickAgain(false);
                    }
                }

            }
        });
        return convertView;
    }

    /**
     * 打开选项
     * @param context
     * @param option
     */
    private static void openOption(Context context, Option option) {
        String type = option.getType();
        switch (type){
            case DECIDE_OPEN_URL:
                openScheme(context,option.getUrl());
                break;
            case DECIDE_REPLY:
                String message = option.getMessage();
                if (!StringUtils.isBlank(message) && NetUtils.isNetworkConnected(context)) {
                    EventBus.getDefault()
                            .post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SEND_ACTION_CONTENT_MESSAGE, message));
                }
                break;
            case DECIDE_BOT_ACTION:
                String triggerId = option.getActionTrigger();
                triggerBotRequest(context,triggerId);
                break;
        }

    }

    /**
     * 触发机器人的请求
     * @param triggerId
     */
    private static void triggerBotRequest(Context context,String triggerId) {
        if(NetUtils.isNetworkConnected(context)){
            ChatAPIService chatAPIService = new ChatAPIService(context);
            chatAPIService.setAPIInterface(new WebService());
            chatAPIService.openDecideBotRequest(triggerId);
        }
    }

    /**
     * 打开scheme
     * @param context
     * @param optionContent
     */
    public static void openScheme(Context context,String optionContent) {
        optionContent = optionContent.trim();
        try {
            if (optionContent.startsWith(DECIDE_HTTP)) {
                Intent intent = new Intent();
                intent.setClass(context, ImpActivity.class);
                intent.putExtra(DECIDE_URI, optionContent);
                intent.putExtra(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
                context.startActivity(intent);
            } else {
                Uri uri = Uri.parse(optionContent);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    static class WebService extends APIInterfaceInstance{
        @Override
        public void returnOpenDecideBotRequestSuccess() {
            super.returnOpenDecideBotRequestSuccess();
        }

        @Override
        public void returnOpenDecideBotRequestFail(String error, int errorCode) {
            super.returnOpenDecideBotRequestFail(error, errorCode);
        }
    }
}
