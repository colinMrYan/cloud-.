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
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

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

    public void createDirectConversation(Activity context, String uid,
                                         OnCreateDirectConversationListener onCreateDirectConversationListener) {
        this.context = context;
        this.onCreateDirectConversationListener = onCreateDirectConversationListener;
        loadingDlg = new LoadingDialog(context);
        loadingDlg.show();
        ChatAPIService apiService = new ChatAPIService(context);
        apiService.setAPIInterface(new WebService());
        apiService.createDirectConversation(uid);
    }

    public void createGroupConversation(Activity context, JSONArray peopleArray,
                                        OnCreateGroupConversationListener onCreateGroupConversationListener) {
        this.context = context;
        this.onCreateGroupConversationListener = onCreateGroupConversationListener;
        JSONArray uidArray = new JSONArray();
        JSONArray nameArray = new JSONArray();
        LogUtils.jasonDebug("peopleArray="+peopleArray.length());
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

    public interface OnCreateDirectConversationListener {
        void createDirectConversationSuccess(Conversation conversation);

        void createDirectConversationFail();
    }

    public interface OnCreateGroupConversationListener {
        void createGroupConversationSuccess(Conversation conversation);

        void createGroupConversationFail();
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnCreateDirectConversationSuccess(Conversation conversation) {
            ConversationCacheUtils.saveConversation(MyApplication.getInstance(),conversation);
            LoadingDialog.dimissDlg(loadingDlg);
            if (onCreateDirectConversationListener != null) {
                onCreateDirectConversationListener.createDirectConversationSuccess(conversation);
            }
        }

        @Override
        public void returnCreateDirectConversationFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(context, error, errorCode);
            if (onCreateDirectConversationListener != null) {
                onCreateDirectConversationListener.createDirectConversationFail();
            }
        }

        @Override
        public void returnCreateGroupConversationSuccess(Conversation conversation) {
            ConversationCacheUtils.saveConversation(MyApplication.getInstance(),conversation);
            LoadingDialog.dimissDlg(loadingDlg);
            if (onCreateGroupConversationListener != null) {
                onCreateGroupConversationListener.createGroupConversationSuccess(conversation);
            }
        }

        @Override
        public void returnCreateGroupConversationFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(context, error, errorCode);
            if (onCreateGroupConversationListener != null) {
                onCreateGroupConversationListener.createGroupConversationFail();
            }
        }


    }
}
