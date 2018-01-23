package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
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

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * 修改共享网盘名称
 *
 */
@ContentView(R.layout.activity_modify_channel_group_name)
public class ShareVolumeNameModifyActivity extends BaseActivity {

	@ViewInject(R.id.edit)
	private ClearEditText editText;

	@ViewInject(R.id.header_text)
	private TextView headerText;

	private LoadingDialog loadingDlg;
	private Volume volume;
	private Group group;
	private boolean isVolumeNameModify = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getIntent().hasExtra("volume")){
            isVolumeNameModify = true;
            volume = (Volume) getIntent().getSerializableExtra("volume");
        }else {
		    group = (Group)getIntent().getSerializableExtra("group");
        }
        headerText.setText(isVolumeNameModify?"更改网盘名称":"更改组名称");
		EditTextUtils.setText(editText, isVolumeNameModify?volume.getName():group.getName());
		loadingDlg= new LoadingDialog(ShareVolumeNameModifyActivity.this);
	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.save_text:
			String name = editText.getText().toString();
			if (StringUtils.isBlank(name)) {
				ToastUtils.show(getApplicationContext(), isVolumeNameModify?"请输入网盘名称":"请输入组名称");
			}else if(isVolumeNameModify){
                if (!FomatUtils.isValidFileName(name)) {
                    ToastUtils.show(getApplicationContext(), "网盘名中不能包含特殊字符 / \\ \" : | * ? < >");
                } else {
                    updateShareVolumeName(name);
                }
            }else {
			    updateGroupName(name);
            }
			break;

		default:
			break;
		}
	}
	/**
	 * 修改网盘名称
	 * @param volume
	 * @param name
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
     * @param groupId
     * @param groupName
     */
	private void updateGroupName(String groupName){
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            MyAppAPIService apiService = new MyAppAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.updateGroupName(group.getId(),groupName);
        }
    }

	private class WebService extends APIInterfaceInstance{
        @Override
        public void returnUpdateShareVolumeNameSuccess(Volume volume, String name) {
           LoadingDialog.dimissDlg(loadingDlg);
            Intent intent = new Intent();
            intent.putExtra("volumeName",name);
            setResult(RESULT_OK,intent);
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
            intent.putExtra("groupName",name);
            setResult(RESULT_OK,intent);
            finish();
        }

        @Override
        public void returnUpdateGroupNameFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
