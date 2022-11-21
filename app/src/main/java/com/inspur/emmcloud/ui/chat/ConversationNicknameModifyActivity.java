package com.inspur.emmcloud.ui.chat;

import android.view.View;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.PinyinUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
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
public class ConversationNicknameModifyActivity extends BaseActivity {

    @BindView(R.id.edit)
    ClearEditText editText;
    private Conversation conversation;
    private LoadingDialog loadingDlg;
    private String name;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        String id = getIntent().getStringExtra("cid");
        name = getIntent().getStringExtra("nickname");
        conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), id);
        if (conversation == null) {
            ToastUtils.show(getApplicationContext(), getString(R.string.net_request_failed));
            finish();
            return;
        }
        EditTextUtils.setText(editText, name);
        loadingDlg = new LoadingDialog(ConversationNicknameModifyActivity.this);
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
                    ToastUtils.show(getApplicationContext(), R.string.group_nickname_cannot_null);
                    return;
                }
                if (name.length() > 16) {
                    ToastUtils.show(getApplicationContext(), R.string.group_nickname_longth_valid);
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
            ChatAPIService apiService = new ChatAPIService(ConversationNicknameModifyActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.updateGroupNickname(conversation.getId(), name);
        }
    }


    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnUpdateConversationNicknameSuccess(Conversation updateConversation) {
            LoadingDialog.dimissDlg(loadingDlg);
            ConversationCacheUtils.saveConversation(BaseApplication.getInstance(), updateConversation);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_NICK_NAME, editText.getText().toString().trim()));
            finish();
        }

        @Override
        public void returnUpdateConversationNicknameFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationNicknameModifyActivity.this, error, errorCode);
        }
    }
}
