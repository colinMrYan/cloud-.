package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * 修改群组名称
 * @author Administrator
 *
 */
@ContentView(R.layout.activity_conversation_name_modify)
public class ConversationNameModifyActivity extends BaseActivity {

	@ViewInject(R.id.edit)
	private ClearEditText editText;
	private Conversation conversation;
	private LoadingDialog loadingDlg;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		String id = getIntent().getStringExtra("cid");
		conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(),id);
		name = conversation.getName();
		EditTextUtils.setText(editText, name);
		loadingDlg= new LoadingDialog(ConversationNameModifyActivity.this);
	}
	
	public void onClick(View v){
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
			if (name.length()>40){
				ToastUtils.show(getApplicationContext(), R.string.group_name_longth_valid);
				return;
			}
			modifyConversationName(name);
			break;

		default:
			break;
		}
	}

	private void modifyConversationName(String name){
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			loadingDlg.show();
			ChatAPIService apiService = new ChatAPIService(ConversationNameModifyActivity.this);
			apiService.setAPIInterface(new WebService());
			apiService.updateConversationName(conversation.getId(), name);
		}
	}


	private class WebService extends APIInterfaceInstance{

		@Override
		public void returnUpdateConversationNameSuccess() {
			LoadingDialog.dimissDlg(loadingDlg);
			conversation.setName(name);
			ConversationCacheUtils.updateConversationName(MyApplication.getInstance(),conversation.getId(),name);
			EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME,conversation));
			finish();
		}

		@Override
		public void returnUpdateConversationNameFail(String error, int errorCode) {
			LoadingDialog.dimissDlg(loadingDlg);
			WebServiceMiddleUtils.hand(ConversationNameModifyActivity.this, error,errorCode);
		}
	}
}
