package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.appcenter.volume.Group;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 修改共享网盘名称
 */
public class ShareVolumeNameModifyActivity extends BaseActivity {

    @BindView(R.id.edit)
    ClearEditText editText;

    @BindView(R.id.header_text)
    TextView headerText;

    private LoadingDialog loadingDlg;
    private Volume volume;
    private Group group;
    private boolean isVolumeNameModify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        if (getIntent().hasExtra("volume")) {
            isVolumeNameModify = true;
            volume = (Volume) getIntent().getSerializableExtra("volume");
        } else {
            group = (Group) getIntent().getSerializableExtra("group");
        }
        headerText.setText(isVolumeNameModify ? R.string.clouddriver_update_volume_name : R.string.clouddriver_update_group_name);
        EditTextUtils.setText(editText, isVolumeNameModify ? volume.getName() : group.getName());
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MyAppConfig.VOLUME_MAX_FILE_NAME_LENGTH)});
        loadingDlg = new LoadingDialog(ShareVolumeNameModifyActivity.this);
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
                String name = editText.getText().toString();
                if (StringUtils.isBlank(name)) {
                    ToastUtils.show(getApplicationContext(), isVolumeNameModify ? R.string.clouddriver_input_volume_name : R.string.clouddriver_input_volume_group_name);
                } else if (isVolumeNameModify) {
                    if (!FomatUtils.isValidFileName(name)) {
                        ToastUtils.show(getApplicationContext(), R.string.clouddriver_volume_name_invaliad);
                    } else {
                        updateShareVolumeName(name);
                    }
                } else {
                    updateGroupName(name);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 修改网盘名称
     *
     * @param
     * @param
     */
    private void updateShareVolumeName(String volumeName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            MyAppAPIService apiService = new MyAppAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.updateShareVolumeName(volume, volumeName);
        }
    }

    /**
     * 修改组名称
     *
     * @param groupName
     */
    private void updateGroupName(String groupName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            MyAppAPIService apiService = new MyAppAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.updateGroupName(group.getId(), groupName);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnUpdateShareVolumeNameSuccess(Volume volume, String name) {
            LoadingDialog.dimissDlg(loadingDlg);
            Intent intent = new Intent();
            intent.putExtra("volumeName", name);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnUpdateShareVolumeNameFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnUpdateGroupNameSuccess(String name) {
            LoadingDialog.dimissDlg(loadingDlg);
            Intent intent = new Intent();
            intent.putExtra("groupName", name);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnUpdateGroupNameFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
