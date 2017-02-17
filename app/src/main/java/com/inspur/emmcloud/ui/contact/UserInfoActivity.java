package com.inspur.emmcloud.ui.contact;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.GetChannelInfoResult;
import com.inspur.emmcloud.bean.GetCreateSingleChannelResult;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ImagePagerActivity;
import com.inspur.emmcloud.util.ChatCreateUtils;
import com.inspur.emmcloud.util.ChatCreateUtils.OnCreateDirectChannelListener;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

public class UserInfoActivity extends BaseActivity {

	private TextView departmentText;
	private TextView mailText;
	private TextView phoneNumText;
	private LoadingDialog loadingDlg;
	private ImageView photoImg;
	private TextView nameText;
	private ImageDisplayUtils imageDisplayUtils;
	private ChatAPIService apiService;
	private Contact contact;
	private final static int MY_PERMISSIONS_PHONECALL = 0;
	private final static int MY_PERMISSIONS_SMS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		((MyApplication) getApplicationContext()).addActivity(this);
//		in(null);
		departmentText = (TextView) findViewById(R.id.department_text);
		mailText = (TextView) findViewById(R.id.mail_text);
		phoneNumText = (TextView) findViewById(R.id.phone_num_text);
		photoImg = (ImageView) findViewById(R.id.photo_img);
		nameText = (TextView) findViewById(R.id.name_text);
		imageDisplayUtils = new ImageDisplayUtils(getApplicationContext(),
				R.drawable.icon_person_default);
		apiService = new ChatAPIService(UserInfoActivity.this);
		apiService.setAPIInterface(new Webservice());
		loadingDlg = new LoadingDialog(UserInfoActivity.this);
		if (getIntent().hasExtra("cid")) {
			getChannelInfo();
		} else {
			String scheme = getIntent().getScheme();
			if (scheme != null) {
				String uri = getIntent().getDataString();
				String uid = uri.split("//")[1];
				contact = ContactCacheUtils.getUserContact(
						getApplicationContext(), uid);
				disPlayUserInfo(contact);
			} else if (getIntent().hasExtra("userInfo")) {
				contact = (Contact) getIntent().getExtras().getSerializable(
						"userInfo");
				disPlayUserInfo(contact);
			} else if (getIntent().hasExtra("uid")) {
				String uid = getIntent().getExtras().getString("uid");
				contact = ContactCacheUtils.getUserContact(
						getApplicationContext(), uid);
				// Contact contact;
				// if (!uid.startsWith("BOT")) {
				//
				// } else {
				// Robot robot = RobotCacheUtils.getRobotById(
				// UserInfoActivity.this, uid);
				// contact = new Contact();
				// contact.setOrgName(robot.getName());
				// contact.setEmail(robot.getSupport());
				// contact.setName(robot.getName());
				// contact.setMobile(robot.getSupport());
				// // 如果是机器人隐藏发起会话信息
				// ((ImageView) findViewById(R.id.start_chat_img))
				// .setVisibility(View.GONE);
				//
				// }
				// if (contact == null) {
				// contact = new Contact();
				// }
			}
			disPlayUserInfo(contact);
		}

	}
	
	   public void in(View v) {
			ComponentName componentName = new ComponentName(
		            "com.myprojectone",
		            "com.myprojectone.MainActivity");
		        Intent intentOpen = new Intent();
//		        Bundle bundle = new Bundle();
//		        bundle.putString("resUrl", resurl);
//		        bundle.putSerializable("picUrlList", picurllist);
//		        intentOpen.putExtras(bundle);
		        intentOpen.setComponent(componentName);
		        startActivity(intentOpen);
	        overridePendingTransition(R.anim.anim_zoom_in,R.anim.anim_zoom_out);
	    }

	    public void out(View v) {
	        overridePendingTransition(R.anim.anim_zoom_in,R.anim.anim_zoom_out);
	    }

	/**
	 * 获取频道信息
	 */
	private void getChannelInfo() {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(UserInfoActivity.this)) {
			String cid = getIntent().getExtras().getString("cid");
			loadingDlg.show();
			apiService.getChannelInfo(cid);
		}

	}

	private void disPlayUserInfo(Contact contact) {

		if (contact == null) {
			contact = new Contact();
		}
		String inspurID = contact.getInspurID();
		String organize = contact.getOrgName();
		String mail = contact.getEmail();
		String phoneNum = contact.getMobile();
		String name = contact.getName();
		String headUrl = UriUtils.getChannelImgUri(contact.getInspurID());
		if (StringUtils.isEmpty(organize)) {
			((LinearLayout) findViewById(R.id.department_layout))
					.setVisibility(View.GONE);
		} else {
			departmentText.setText(organize);
		}

		if (StringUtils.isEmpty(mail)) {
			((LinearLayout) findViewById(R.id.mail_layout))
					.setVisibility(View.GONE);
		} else {
			mailText.setText(mail);
		}

		if (StringUtils.isEmpty(phoneNum)) {
			((RelativeLayout) findViewById(R.id.contact_layout))
					.setVisibility(View.GONE);
		} else {
			phoneNumText.setText(phoneNum);
		}

		if (StringUtils.isEmpty(name)) {
			nameText.setText(getString(R.string.not_set));
		} else {
			nameText.setText(name);
		}
		imageDisplayUtils.display(photoImg, headUrl);
		String myUid = ((MyApplication)getApplicationContext()).getUid();
		if (StringUtils.isBlank(inspurID)|| inspurID.equals(myUid)) {
			((ImageView) findViewById(R.id.start_chat_img))
					.setVisibility(View.GONE);
		}

	}


	public void onClick(View v) {
		String phoneNum = phoneNumText.getText().toString();
		String mail = mailText.getText().toString();
		switch (v.getId()) {
		case R.id.mail_img:
			sendMail(mail);
			break;
		case R.id.phone_img:
			// 取消申请权限
			// if (isMobileSet) {
			// if (ContextCompat.checkSelfPermission(UserInfoActivity.this,
			// Manifest.permission.CALL_PHONE) !=
			// PackageManager.PERMISSION_GRANTED) {
			//
			// ActivityCompat.requestPermissions(UserInfoActivity.this,
			// new String[]{Manifest.permission.CALL_PHONE},
			// MY_PERMISSIONS_PHONECALL);
			// } else {
			call(phoneNum);
			// }
			// }
			break;
		case R.id.short_msg_img:
			// if (isMobileSet) {
			// if (ContextCompat.checkSelfPermission(UserInfoActivity.this,
			// Manifest.permission.SEND_SMS) !=
			// PackageManager.PERMISSION_GRANTED) {
			//
			// ActivityCompat.requestPermissions(UserInfoActivity.this,
			// new String[]{Manifest.permission.SEND_SMS},
			// MY_PERMISSIONS_SMS);
			// } else {
			sendSMS(phoneNum);
			// }
			// }
			break;
		case R.id.back_layout:
			finish();
			break;
		case R.id.photo_img:
			Intent intent = new Intent(UserInfoActivity.this,
					ImagePagerActivity.class);
			ArrayList<String> urls = new ArrayList<String>();
			urls.add(UriUtils.getChannelImgUri(contact.getInspurID()));
			intent.putExtra("image_index", 0);
			intent.putStringArrayListExtra("image_urls", urls);
			startActivity(intent);
			break;
		case R.id.start_chat_img:
			createDireactChannel();
			break;
		default:
			break;
		}
	}

	/**
	 * 创建单聊
	 */
	private void createDireactChannel() {
		// TODO Auto-generated method stub
		new ChatCreateUtils().createDirectChannel(UserInfoActivity.this, contact.getInspurID(),
				new OnCreateDirectChannelListener() {

					@Override
					public void createDirectChannelSuccess(
							GetCreateSingleChannelResult getCreateSingleChannelResult) {
						// TODO Auto-generated method stub
						Bundle bundle = new Bundle();
						bundle.putString("channelId",
								getCreateSingleChannelResult.getCid());
						bundle.putString("channelType",
								getCreateSingleChannelResult.getType());
						bundle.putString("title", getCreateSingleChannelResult
								.getName(getApplicationContext()));
						LogUtils.JasonDebug("title="+getCreateSingleChannelResult
								.getName(getApplicationContext()));
						IntentUtils.startActivity(UserInfoActivity.this,
								ChannelActivity.class, bundle);
					}

					@Override
					public void createDirectChannelFail() {
						// TODO Auto-generated method stub

					}
				});
	}

	public void onBack(View v) {
		finish();
	}

	private void sendSMS(String phoneNum) {
		Uri smsToUri = Uri.parse("smsto:" + phoneNum);
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		startActivity(intent);

	}

	private void call(String phoneNum) {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ phoneNum));

		startActivity(intent);

	}

	// 取消申请权限
	// /**
	// * 授权回调方法
	// */
	// @Override
	// public void onRequestPermissionsResult(int requestCode,
	// String permissions[], int[] grantResults) {
	// String phoneNum = phoneNumText.getText().toString();
	// switch (requestCode) {
	// case MY_PERMISSIONS_PHONECALL:
	// // If request is cancelled, the result arrays are empty.
	// if (grantResults.length > 0
	// && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	// call(phoneNum);
	// } else {
	// Toast.makeText(UserInfoActivity.this, "未授权拨打电话",
	// Toast.LENGTH_SHORT).show();
	// }
	// break;
	// case MY_PERMISSIONS_SMS:
	// if (grantResults.length > 0
	// && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	// sendSMS(phoneNum);
	// } else {
	// Toast.makeText(UserInfoActivity.this, "未授权发送短信",
	// Toast.LENGTH_SHORT).show();
	// }
	// break;
	//
	// }
	// }

	private void sendMail(String mail) {
		Uri uri = Uri.parse("mailto:" + mail);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		startActivity(Intent.createChooser(intent,
				getString(R.string.please_select_app_of_mail)));
	}

	public void dimissDlg() {
		if (loadingDlg != null && loadingDlg.isShowing()) {
			loadingDlg.dismiss();
		}
	}

	private class Webservice extends APIInterfaceInstance {

		@Override
		public void returnChannelInfoSuccess(
				GetChannelInfoResult getChannelInfoResult) {
			// TODO Auto-generated method stub
			dimissDlg();
			JSONArray membersArray = getChannelInfoResult.getMembersArray();
			String myUid = PreferencesUtils.getString(UserInfoActivity.this,
					"userID", "");
			if (membersArray != null && membersArray.length() >= 2) {
				try {
					String uid = "";
					if (membersArray.getString(0).equals(myUid)) {
						uid = membersArray.getString(1);
					} else {
						uid = membersArray.getString(0);
					}
					contact = ContactCacheUtils.getUserContact(
							getApplicationContext(), uid);
					disPlayUserInfo(contact);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			}

		}

		@Override
		public void returnChannelInfoFail(String error) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(UserInfoActivity.this, error);
		}

	}
}
