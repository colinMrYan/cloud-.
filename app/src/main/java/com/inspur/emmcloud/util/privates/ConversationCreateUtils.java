/**
 * ChatCreateUtils.java
 * classes : com.inspur.emmcloud.util.privates.ChatCreateUtils
 * V 1.0.0
 * Create at 2016年11月29日 下午7:44:41
 */
package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.OnCreateGroupConversationListener;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * com.inspur.emmcloud.util.privates.ChatCreateUtils create at 2016年11月29日 下午7:44:41
 */
public class ConversationCreateUtils {
    private Context context;
    private OnCreateDirectConversationListener onCreateDirectConversationListener;
    private OnCreateGroupConversationListener onCreateGroupConversationListener;
    private LoadingDialog loadingDlg;
    private boolean isShowErrorAlert = true;

    public void createDirectConversation(Activity context, String uid,
                                         OnCreateDirectConversationListener onCreateDirectConversationListener) {
        createDirectConversation(context, uid, onCreateDirectConversationListener, true);
    }

    public void createDirectConversation(Activity context, String uid,
                                         OnCreateDirectConversationListener onCreateDirectConversationListener, boolean isShowErrorAlert) {
        this.context = context;
        this.onCreateDirectConversationListener = onCreateDirectConversationListener;
        this.isShowErrorAlert = isShowErrorAlert;
        loadingDlg = new LoadingDialog(context);
        loadingDlg.show();
        ChatAPIService apiService = new ChatAPIService(context);
        apiService.setAPIInterface(new WebService());
        apiService.createDirectConversation(uid);
    }

    public void createGroupConversation(Activity context, JSONArray peopleArray,
                                        OnCreateGroupConversationListener onCreateGroupConversationListener) {
        createGroupConversation(context, getLastSortedSearchMembers(peopleArray,true), null, onCreateGroupConversationListener);
    }

    //增加群聊名称  会议过来的使用会议名称
    public void createGroupConversation(Activity context, JSONArray peopleArray, String groupName,
                                        OnCreateGroupConversationListener onCreateGroupConversationListener) {
        this.context = context;
        this.onCreateGroupConversationListener = onCreateGroupConversationListener;
        JSONArray uidArray = new JSONArray();
        JSONArray nameArray = new JSONArray();
        LogUtils.jasonDebug("peopleArray=" + peopleArray.length());
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
        if (StringUtils.isBlank(groupName)) {
            groupName = createChannelGroupName(nameArray);
        }
        loadingDlg = new LoadingDialog(context);
        loadingDlg.show();
        ChatAPIService apiService = new ChatAPIService(context);
        apiService.setAPIInterface(new WebService());
        uidArray.put(MyApplication.getInstance().getUid());
        apiService.createGroupConversation(groupName, uidArray);
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

    //群聊用户排序
    public static JSONArray getLastSortedSearchMembers(JSONArray peopleArray, boolean withOwner) {
        JSONArray finalPeopleArray = new JSONArray();
        Map<Integer, String> map = new TreeMap<>();
        if (withOwner) {
            map.put(Integer.valueOf(MyApplication.getInstance().getUid()), PreferencesUtils.getString(BaseApplication.getInstance(), "userRealName", ""));
        }
        try {
            for (int i = 0; i < peopleArray.length(); i++) {
                JSONObject peopleObj = peopleArray.getJSONObject(i);
                if (peopleObj.getString("pid") != null) {
                    map.put(Integer.valueOf(getUidFromIdInfo(peopleObj.getString("pid"))), peopleObj.getString("name"));
                }
            }
            TreeMap<Integer, String> sortedMap = new TreeMap<>(map);
            if (sortedMap.size() != 0){
                for (Map.Entry<Integer, String> entry : sortedMap.entrySet()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("pid", entry.getKey().toString());
                    jsonObject.put("name", entry.getValue());
                    finalPeopleArray.put(jsonObject);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalPeopleArray;
    }

    //获取兼职账号对应的主账号id
    public static String getUidFromIdInfo(String uid) {
        String finalUid = uid;
        int SUB_USER_ADD_LENGTH = ((int)Math.pow(10,8) + "").length();
        int uidLength = uid.length();
        if (uid.length() > SUB_USER_ADD_LENGTH) {
            finalUid = uid.substring(0, uidLength - SUB_USER_ADD_LENGTH).trim();
        }
        return finalUid;
    }

//    public interface OnCreateDirectConversationListener {
//        void createDirectConversationSuccess(Conversation conversation);
//
//        void createDirectConversationFail();
//    }
//
//    public interface OnCreateGroupConversationListener {
//        void createGroupConversationSuccess(Conversation conversation);
//
//        void createGroupConversationFail();
//    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnCreateDirectConversationSuccess(Conversation conversation) {
            ConversationCacheUtils.saveConversation(MyApplication.getInstance(), conversation);
            LoadingDialog.dimissDlg(loadingDlg);
            if (onCreateDirectConversationListener != null) {
                onCreateDirectConversationListener.createDirectConversationSuccess(conversation);
            }
        }

        @Override
        public void returnCreateDirectConversationFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (isShowErrorAlert) {
                WebServiceMiddleUtils.hand(context, error, errorCode);
            }
            if (onCreateDirectConversationListener != null) {
                onCreateDirectConversationListener.createDirectConversationFail();
            }
        }

        @Override
        public void returnCreateGroupConversationSuccess(Conversation conversation) {
            ConversationCacheUtils.saveConversation(MyApplication.getInstance(), conversation);
            LoadingDialog.dimissDlg(loadingDlg);
            if (onCreateGroupConversationListener != null) {
                onCreateGroupConversationListener.createGroupConversationSuccess(conversation);
            }
        }

        @Override
        public void returnCreateGroupConversationFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (isShowErrorAlert) {
                WebServiceMiddleUtils.hand(context, error, errorCode);
            }
            if (onCreateGroupConversationListener != null) {
                onCreateGroupConversationListener.createGroupConversationFail();
            }
        }


    }
}
