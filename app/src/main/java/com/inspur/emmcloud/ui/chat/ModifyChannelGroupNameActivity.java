package com.inspur.emmcloud.ui.chat;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

/**
 * 修改群组名称
 *
 * @author Administrator
 */
public class ModifyChannelGroupNameActivity extends BaseActivity {

    private ClearEditText editText;
    private String cid;
    private LoadingDialog loadingDlg;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        editText = (ClearEditText) findViewById(R.id.edit);
        String name = getIntent().getStringExtra("name");
        cid = getIntent().getStringExtra("cid");
        EditTextUtils.setText(editText, name);
        loadingDlg = new LoadingDialog(ModifyChannelGroupNameActivity.this);
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
                if (name.length() > 40) {
                    ToastUtils.show(getApplicationContext(), R.string.group_name_longth_valid);
                    return;
                }
                if (NetUtils.isNetworkConnected(getApplicationContext())) {
                    loadingDlg.show();
                    ChatAPIService apiService = new ChatAPIService(ModifyChannelGroupNameActivity.this);
                    apiService.setAPIInterface(new WebService());
                    apiService.updateChannelGroupName(cid, name);
                }
                break;

            default:
                break;
        }
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnUpdateChannelGroupNameSuccess(
                GetBoolenResult getBoolenResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            Intent intent = new Intent("message_notify");
            intent.putExtra("command", "refresh_session_list");
            LocalBroadcastManager.getInstance(ModifyChannelGroupNameActivity.this).sendBroadcast(intent);

            Intent intentChannel = new Intent("update_channel_name");
            intentChannel.putExtra("name", name);
            LocalBroadcastManager.getInstance(ModifyChannelGroupNameActivity.this).sendBroadcast(intentChannel);

            Intent intentChannelInfo = new Intent();
            intentChannelInfo.putExtra("name", name);
            ModifyChannelGroupNameActivity.this.setResult(RESULT_OK, intentChannelInfo);
            finish();
        }

        @Override
        public void returnUpdateChannelGroupNameFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(ModifyChannelGroupNameActivity.this, error, errorCode);
        }

    }
}
