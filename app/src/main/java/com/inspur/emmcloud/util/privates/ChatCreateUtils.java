/**
 * ChatCreateUtils.java
 * classes : com.inspur.emmcloud.util.privates.ChatCreateUtils
 * V 1.0.0
 * Create at 2016年11月29日 下午7:44:41
 */
package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;

import org.json.JSONArray;
import org.json.JSONException;

//import com.inspur.emmcloud.bean.schedule.Participant;
//import com.inspur.emmcloud.bean.schedule.Schedule;

/**
 * com.inspur.emmcloud.util.privates.ChatCreateUtils create at 2016年11月29日 下午7:44:41
 */
public class ChatCreateUtils {
    JSONArray peopleArray;
    ICreateGroupChatListener iCreateGroupChatListener;
    //    Schedule meeting;
    private Context context;
    private OnCreateDirectChannelListener onCreateDirectChannelListener;
    private OnCreateGroupChannelListener onCreateGroupChannelListener;
    private LoadingDialog loadingDlg;
    private boolean isShowErrorAlert = true;
//    private ScheduleApiService scheduleApiService;

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
//        if (meeting != null && !StringUtils.isBlank(meeting.getTitle())) {
//            groupName = meeting.getTitle();
//        }
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

//    /**
//     * 发起群聊  入口
//     *
//     * @param meeting  会议对象
//     * @param chatGroupId CID 群聊ID
//     * @param listener    成功失败回调 可以传null
//     */
//    public void startGroupChat(Activity context, Schedule meeting, String chatGroupId, ICreateGroupChatListener listener) {
//        this.context = context;
//        this.iCreateGroupChatListener = listener;
//
//        scheduleApiService = new ScheduleApiService(context);
//        scheduleApiService.setAPIInterface(new WebService());
//        this.meeting = meeting;
//        if (meeting == null) return;
//        peopleArray = getPeopleArray(meeting);
//        if (peopleArray.length() < 2) {
//            ToastUtils.show(R.string.chat_group_least_two_person);
//            return;
//        }
//
//        if (StringUtils.isBlank(chatGroupId)) {
//            loadingDlg = new LoadingDialog(context);
//            loadingDlg.show();
//            scheduleApiService.getCalendarBindChat(meeting.getId());
//        } else {
//            if (TabAndAppExistUtils.isTabExist(context, Constant.APP_TAB_BAR_COMMUNACATE)) {
//                Bundle bundle = new Bundle();
//                bundle.putString(ConversationActivity.EXTRA_CID, chatGroupId);
//                IntentUtils.startActivity(context, ConversationActivity.class, bundle);
//            }
//        }
//    }
//
//    private JSONArray getPeopleArray(Schedule meeting) {
//        List<Participant> totalList = deleteRepeatData(meeting.getAllParticipantList(), meeting.getOwner());
//        JSONArray peopleArray = new JSONArray();
//        for (Participant participant : totalList) {
//            JSONObject json = new JSONObject();
//            try {
////                if (!participant.getId().equals(BaseApplication.getInstance().getUid())) {
//                json.put("pid", participant.getId());
//                json.put("name", participant.getName());
//                peopleArray.put(json);
////                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return peopleArray;
//    }
//
//    //list去重
//    private List<Participant> deleteRepeatData(List<Participant> list, String owner) {
//        //把创建人加入到群聊
//        if (!StringUtils.isBlank(owner)) {
//            Participant ownerParticipant = new Participant();
//            ownerParticipant.setId(owner);
//            String ownerName = ContactUserCacheUtils.getUserName(owner);
//            ownerParticipant.setName(ownerName);
//            list.add(ownerParticipant);
//        }
//        //去除通讯录中不存在的人(外部联系人)
//        Iterator<Participant> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            Participant item = iterator.next();
//            ContactUser user = ContactUserCacheUtils.getContactUserByUid(item.getId());
//            if (user == null) {
//                iterator.remove();
//            }
//        }
//        Set<Participant> set = new TreeSet<>(new Comparator<Participant>() {
//            @Override
//            public int compare(Participant o1, Participant o2) {
//                return o1.getId().compareTo(o2.getId());
//            }
//        });
//        set.addAll(list);
//        List<Participant> result = new ArrayList<>(set);
//        Collections.reverse(result);
//
//        return result;
//    }

    public interface OnCreateDirectChannelListener {
        void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult);

        void createDirectChannelFail();
    }

    public interface OnCreateGroupChannelListener {
        void createGroupChannelSuccess(ChannelGroup channelGroup);

        void createGroupChannelFail();
    }

    public interface ICreateGroupChatListener {
        void createSuccess();

        void createFail();
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
        public void returnCreateSingleChannelFail(String error, int errorCode) {
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
        public void returnCreateChannelGroupSuccess(ChannelGroup channelGroup) {
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

//        @Override
//        public void returnGetCalendarChatBindSuccess(final String calendar, String cid) {
//            if (StringUtils.isBlank(cid)) { //新建群聊
//                String groupName = null;
//                if (meeting != null && !StringUtils.isBlank(meeting.getTitle())) {
//                    groupName = meeting.getTitle();
//                }
//                new ConversationCreateUtils().createGroupConversation((Activity) context, peopleArray, groupName, new ConversationCreateUtils.OnCreateGroupConversationListener() {
//                    @Override
//                    public void createGroupConversationSuccess(Conversation conversation) {
//                        if (loadingDlg != null) {
//                            loadingDlg.dismiss();
//                        }
//                        if (iCreateGroupChatListener != null) {
//                            iCreateGroupChatListener.createSuccess();  //创建成功回调
//                        }
//                        scheduleApiService.setCalendarBindChat(calendar, conversation.getId());
//                        if (TabAndAppExistUtils.isTabExist(context, Constant.APP_TAB_BAR_COMMUNACATE)) {
//                            Bundle bundle = new Bundle();
//                            bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
//                            IntentUtils.startActivity((Activity) context, ConversationActivity.class, bundle);
//                            //创建群聊成功后  通知消息界面刷新界面数据（群组头像等）
//                            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CHAT_CHANGE, conversation));
//                        }
//                    }
//
//                    @Override
//                    public void createGroupConversationFail() {
//                        if (iCreateGroupChatListener != null) {
//                            iCreateGroupChatListener.createFail();  //创建失败回调
//                        }
//                        if (loadingDlg != null) {
//                            loadingDlg.dismiss();
//                        }
//                    }
//                });
//            } else {
//                if (loadingDlg != null) {
//                    loadingDlg.dismiss();
//                }
//                if (TabAndAppExistUtils.isTabExist(context, Constant.APP_TAB_BAR_COMMUNACATE)) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString(ConversationActivity.EXTRA_CID, cid);
//                    IntentUtils.startActivity((Activity) context, ConversationActivity.class, bundle);
//                }
//            }
//        }
//
//        //获取群聊cid
//        @Override
//        public void returnSetCalendarChatBindSuccess(String calendarId, String chatId) {
//            if (loadingDlg != null) {
//                loadingDlg.dismiss();
//            }
//            if (TabAndAppExistUtils.isTabExist(context, Constant.APP_TAB_BAR_COMMUNACATE)) {
//                Bundle bundle = new Bundle();
//                bundle.putString(ConversationActivity.EXTRA_CID, chatId);
//                IntentUtils.startActivity((Activity) context, ConversationActivity.class, bundle);
//            }
//        }
//
//        @Override
//        public void returnGetCalendarChatBindFail(String error, int errorCode) {
//            if (iCreateGroupChatListener != null) {
//                iCreateGroupChatListener.createFail();  //创建失败回调
//            }
//            if (loadingDlg != null) {
//                loadingDlg.dismiss();
//            }
//        }
//
//        @Override
//        public void returnSetCalendarChatBindFail(String error, int errorCode) {
//            if (iCreateGroupChatListener != null) {
//                iCreateGroupChatListener.createFail();  //创建失败回调
//            }
//            if (loadingDlg != null) {
//                loadingDlg.dismiss();
//            }
//        }
    }
}
