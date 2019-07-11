/**
 * ChatCreateUtils.java
 * classes : com.inspur.emmcloud.util.privates.ChatCreateUtils
 * V 1.0.0
 * Create at 2016年11月29日 下午7:44:41
 */
package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.ui.chat.ConversationActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * com.inspur.emmcloud.util.privates.ChatCreateUtils create at 2016年11月29日 下午7:44:41
 */
public class ChatCreateUtils {
    private Context context;
    private OnCreateDirectChannelListener onCreateDirectChannelListener;
    private OnCreateGroupChannelListener onCreateGroupChannelListener;
    private LoadingDialog loadingDlg;
    private boolean isShowErrorAlert = true;
    JSONArray peopleArray;
    private ScheduleApiService scheduleApiService;

    public void createDirectChannel(Activity context, String uid,
                                    OnCreateDirectChannelListener onCreateDirectChannelListener) {
        this.context = context;
        this.onCreateDirectChannelListener = onCreateDirectChannelListener;
        loadingDlg = new LoadingDialog(context);
        loadingDlg.show();
        ChatAPIService apiService = new ChatAPIService(context);
        apiService.setAPIInterface(new WebService());
        apiService.createDirectChannel(uid);
    }

    public void createDirectChannel(Activity context, String uid,
                                    OnCreateDirectChannelListener onCreateDirectChannelListener, boolean isShowErrorAlert) {
        this.context = context;
        this.onCreateDirectChannelListener = onCreateDirectChannelListener;
        loadingDlg = new LoadingDialog(context);
        loadingDlg.show();
        ChatAPIService apiService = new ChatAPIService(context);
        apiService.setAPIInterface(new WebService());
        apiService.createDirectChannel(uid);
    }

    public void createGroupChannel(Activity context, JSONArray peopleArray,
                                   OnCreateGroupChannelListener onCreateGroupChannelListener) {
        this.context = context;
        this.onCreateGroupChannelListener = onCreateGroupChannelListener;
        JSONArray uidArray = new JSONArray();
        JSONArray nameArray = new JSONArray();
        for (int i = 0; i < peopleArray.length(); i++) {
            try {
                uidArray.put(i, peopleArray.getJSONObject(i).getString("pid"));

                nameArray
                        .put(i, peopleArray.getJSONObject(i).getString("name"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        String groupName = createChannelGroupName(nameArray);
        LogUtils.jasonDebug("groupName=" + groupName);
        loadingDlg = new LoadingDialog(context);
        loadingDlg.show();
        ChatAPIService apiService = new ChatAPIService(context);
        apiService.setAPIInterface(new WebService());
        apiService.createGroupChannel(groupName, uidArray);

    }

    /**
     * 获取群组名称
     *
     * @param nameArray
     * @return//群组名称最多显示5人人名
     */
    private String createChannelGroupName(JSONArray nameArray) {
        // TODO Auto-generated method stub
        StringBuilder nameBuilder = new StringBuilder();
        String myName = PreferencesUtils.getString(context, "userRealName");
        int length = Math.min(4, nameArray.length());
        nameBuilder.append(myName);
        for (int i = 0; i < length; i++) {
            String name = "";
            try {
                name = nameArray.getString(i);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            nameBuilder.append("、" + name);
        }
        if (nameArray.length() > 4) {
            nameBuilder.append("...");
        }
        return nameBuilder.toString();
    }

    public interface OnCreateDirectChannelListener {
        void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult);

        void createDirectChannelFail();
    }

    public interface OnCreateGroupChannelListener {
        void createGroupChannelSuccess(ChannelGroup channelGroup);

        void createGroupChannelFail();
    }

    public void startGroupChat(Activity context, String scheduleId, JSONArray groupArray) {
        startGroupChat(context, scheduleId, "", groupArray);
    }

    public void startGroupChat(Activity context, String scheduleId, String chatGroupId, JSONArray groupArray) {
        scheduleApiService = new ScheduleApiService(context);
        scheduleApiService.setAPIInterface(new WebService());
        peopleArray = groupArray;

        if (StringUtils.isBlank(chatGroupId)) {
            scheduleApiService.getCalendarBindChat(scheduleId);
        } else {
            if (TabAndAppExistUtils.isTabExist(context, Constant.APP_TAB_BAR_COMMUNACATE)) {
                Bundle bundle = new Bundle();
                bundle.putString(ConversationActivity.EXTRA_CID, chatGroupId);
                IntentUtils.startActivity(context, ConversationActivity.class, bundle);
            }
        }
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnCreateSingleChannelSuccess(
                GetCreateSingleChannelResult getCreateSingleChannelResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (onCreateDirectChannelListener != null) {
                String cid = getCreateSingleChannelResult.getCid();
                onCreateDirectChannelListener.createDirectChannelSuccess(getCreateSingleChannelResult);
            }

        }

        @Override
        public void returnCreatSingleChannelFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (isShowErrorAlert) {
                WebServiceMiddleUtils.hand(context, error, errorCode);
            }
            if (onCreateDirectChannelListener != null) {
                onCreateDirectChannelListener.createDirectChannelFail();
            }

        }

        @Override
        public void returnCreatChannelGroupSuccess(ChannelGroup channelGroup) {
            // TODO Auto-generated method stub

            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (onCreateGroupChannelListener != null) {
                onCreateGroupChannelListener
                        .createGroupChannelSuccess(channelGroup);
            }
        }

        @Override
        public void returnCreateChannelGroupFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (isShowErrorAlert) {
                WebServiceMiddleUtils.hand(context, error, errorCode);
            }
            if (onCreateGroupChannelListener != null) {
                onCreateGroupChannelListener.createGroupChannelFail();
            }
        }

        @Override
        public void returnGetCalendarChatBindSuccess(final String calendar, String cid) {
            if (StringUtils.isBlank(cid)) { //新建群聊
                new ConversationCreateUtils().createGroupConversation((Activity) context, peopleArray, new ConversationCreateUtils.OnCreateGroupConversationListener() {
                    @Override
                    public void createGroupConversationSuccess(Conversation conversation) {
                        scheduleApiService.setCalendarBindChat(calendar, conversation.getId());
                        if (TabAndAppExistUtils.isTabExist(context, Constant.APP_TAB_BAR_COMMUNACATE)) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                            IntentUtils.startActivity((Activity) context, ConversationActivity.class, bundle);
                            //创建群聊成功后  通知消息界面刷新界面数据（群组头像等）
                            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CHAT_CHANGE, conversation));
                        }
                    }

                    @Override
                    public void createGroupConversationFail() {
                    }
                });
            } else {
                if (TabAndAppExistUtils.isTabExist(context, Constant.APP_TAB_BAR_COMMUNACATE)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(ConversationActivity.EXTRA_CID, cid);
                    IntentUtils.startActivity((Activity) context, ConversationActivity.class, bundle);
                }
            }
        }

        //获取群聊cid
        @Override
        public void returnSetCalendarChatBindSuccess(String calendarId, String chatId) {

        }
    }
}
