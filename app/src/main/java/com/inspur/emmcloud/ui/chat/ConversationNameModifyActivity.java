package com.inspur.emmcloud.ui.chat;

import android.view.View;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.PinyinUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 修改群组名称
 *
 * @author Administrator
 */
public class ConversationNameModifyActivity extends BaseActivity {

    @BindView(R.id.edit)
    ClearEditText editText;
    private Conversation conversation;
    private LoadingDialog loadingDlg;
    private String name;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        String id = getIntent().getStringExtra("cid");
        conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), id);
        name = conversation.getName();
        EditTextUtils.setText(editText, name);
        loadingDlg = new LoadingDialog(ConversationNameModifyActivity.this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_name_modify;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.save_text:
                name = editText.getText().toString().trim();
                if (StringUtils.isBlank(name)) {
                    ToastUtils.show(getApplicationContext(), R.string.group_name_cannot_null);
                    return;
                }
                if (name.length() > 25) {
                    ToastUtils.show(getApplicationContext(), R.string.group_name_longth_valid);
                    return;
                }
                modifyConversationName(name);
                break;

            default:
                break;
        }
    }

    private void modifyConversationName(String name) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationNameModifyActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.updateConversationName(conversation.getId(), name);
        }
    }


    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnUpdateConversationNameSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            conversation.setName(name);
            conversation.setShowName(name);
            ConversationCacheUtils.saveConversation(BaseApplication.getInstance(), conversation);
            String pinYin = PinyinUtils.getPingYin(name);
            ConversationCacheUtils.updateConversationPyFull(MyApplication.getInstance(), conversation.getId(), pinYin);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME, conversation));
            finish();
        }

        @Override
        public void returnUpdateConversationNameFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationNameModifyActivity.this, error, errorCode);
        }
    }
}
