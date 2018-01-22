package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		headerText.setText("更改网盘名称");
		editText = (ClearEditText)findViewById(R.id.edit);
		volume = (Volume) getIntent().getSerializableExtra("volume");
		EditTextUtils.setText(editText, volume.getName());
		loadingDlg= new LoadingDialog(ShareVolumeNameModifyActivity.this);
	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.save_text:
			String volumeName = editText.getText().toString();
			if (StringUtils.isBlank(volumeName)) {
				ToastUtils.show(getApplicationContext(), "请输入网盘名称");
			} else if (!FomatUtils.isValidFileName(volumeName)) {
				ToastUtils.show(getApplicationContext(), "网盘名中不能包含特殊字符 / \\ \" : | * ? < >");
			} else {
				updateShareVolumeName(volumeName);
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

	private class WebService extends APIInterfaceInstance{
        @Override
        public void returnUpdateShareVolumeNameSuccess(Volume volume, String name) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            Intent intent = new Intent();
            intent.putExtra("volumeName",name);
            setResult(RESULT_OK,intent);
            finish();
        }

        @Override
        public void returnUpdateShareVolumeNameFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

	}
}
