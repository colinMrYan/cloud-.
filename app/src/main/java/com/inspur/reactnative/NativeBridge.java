package com.inspur.reactnative;

import android.app.Activity;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by yufuchang on 2017/2/20.
 */

public class NativeBridge extends ReactContextBaseJavaModule implements ActivityEventListener{

	private static final int CONTACT_PICKER = 2;
	private Promise promise;
	private boolean multi;

	public NativeBridge(ReactApplicationContext reactContext) {
		super(reactContext);
		reactContext.addActivityEventListener(this);
	}

	@Override
	public String getName() {
		return "NativeBridge";
	}

	@Override
	public Map<String, Object> getConstants() {
		final Map<String, Object> constants = new HashMap<>();
		return constants;
	}

	/**
	 * 获取token
	 *
	 * @return
	 */
	public String getToken() {
		String token = PreferencesUtils.getString(getReactApplicationContext(), "accessToken", "");
		if (StringUtils.isBlank(token)) {
			return null;
		}
		return "Bearer" + " " + token;
	}

	/**
	 * 获取token
	 *
	 * @param promise
	 */
	@ReactMethod
	public void getOAuth20AccessToken(Promise promise) {
		try {
			promise.resolve(getToken());
		} catch (IllegalViewOperationException e) {
			promise.reject(e);
		}
	}

	/**
	 * 获取Profile
	 *
	 * @param promise
	 */
	@ReactMethod
	public void getCurrentUserProfie(Promise promise) {
		String myInfo = PreferencesUtils.getString(getReactApplicationContext(),
				"myInfo", "");
		try {
			JSONObject myprofile = new JSONObject(myInfo);
			promise.resolve(myprofile);
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	/**
	 * 获取企业信息
	 *
	 * @param promise
	 */
	@ReactMethod
	public void getCurrentEnterprise(Promise promise) {
		String myInfo = PreferencesUtils.getString(getReactApplicationContext(),
				"myInfo", "");
		GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", getMyInfoResult.getEnterpriseId());
			jsonObject.put("name", getMyInfoResult.getEnterpriseName());
			jsonObject.put("code", getMyInfoResult.getEnterpriseCode());
			promise.resolve(jsonObject);
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	/**
	 * 通讯录选人
	 * @param multi  是否多选
	 * @param promise
	 */
	@ReactMethod
	public void openContactsPicker(boolean multi,Promise promise) {
		this.promise = promise;
		this.multi = multi;
		Intent intent = new Intent();
		intent.setClass(getReactApplicationContext(),
				ContactSearchActivity.class);
		intent.putExtra("select_content", 2);
		intent.putExtra("isMulti_select", multi);
		intent.putExtra("title",getReactApplicationContext().getString(R.string.adress_list));
		getCurrentActivity().startActivityForResult(intent,CONTACT_PICKER);

	}

	/**
	 * 结束
	 */
	@ReactMethod
	public void exit() {
		getCurrentActivity().finish();
	}

	@Override
	public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		if (requestCode == CONTACT_PICKER){
			if (promise != null){
				if (resultCode == RESULT_OK){
					List<SearchModel> searchModelList = (List<SearchModel>) data.getSerializableExtra("selectMemList");
					List<String> uidList = new ArrayList<>();
					for (int i = 0;i<searchModelList.size();i++){
						String contactId = searchModelList.get(i).getId();
						uidList.add(contactId);
					}
					List<Contact> contactList = ContactCacheUtils.getSoreUserList(activity,uidList);
					if (multi){
						JSONArray jsonArray = new JSONArray();
						for (int i = 0;i<contactList.size();i++){
							JSONObject obj = contactList.get(i).contact2JSONObject();
							jsonArray.put(obj);
						}
						promise.resolve(jsonArray);
					}else {
						JSONObject obj = contactList.get(0).contact2JSONObject();
						promise.resolve(obj);
					}

				}else if(resultCode == RESULT_CANCELED){
					promise.reject("picker was cancelled");
				}
			}

		}

	}

	/**
	 * 根据email查找联系人接口
	 * @param email
	 * @param promise
     */
	@ReactMethod
	public void findContactByMail(String email,Promise promise){
		Contact contact = ContactCacheUtils.getContactByEmail(getCurrentActivity(),email);
		if(contact != null){
			String jsonObject = JSON.toJSONString(contact);
			promise.resolve(jsonObject);
		}else{
			promise.reject("");
		}
	}

	@Override
	public void onNewIntent(Intent intent) {

	}
}
