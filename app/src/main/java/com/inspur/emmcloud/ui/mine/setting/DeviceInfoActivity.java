package com.inspur.emmcloud.ui.mine.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.ToastUtils;

/**
 * Created by Administrator on 2017/5/15.
 */

public class DeviceInfoActivity extends BaseActivity {

	private TextView deviceIdText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_device_info);
		initView();
	}

	private void initView(){
		deviceIdText = (TextView)findViewById(R.id.device_id_text);

	}

	public void onClick(View v){
		switch (v.getId()){
			case R.id.back_layout:
				finish();
				break;
			case R.id.device_unbound_btn:
				break;
			case R.id.device_id_text:
				ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
				cmb.setPrimaryClip(ClipData.newPlainText(null, deviceIdText.getText()));
				ToastUtils.show(this,R.string.copyed_to_paste_board);
				break;
			default:
				break;
		}
	}
}
