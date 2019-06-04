package com.inspur.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.bean.Enterprise;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.mine.GetMyInfoResultWithoutSerializable;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.dialogs.CustomDialog;
import com.inspur.reactnative.bean.AlertButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by yufuchang on 2017/2/20.
 */

public class NativeBridge extends ReactContextBaseJavaModule implements ActivityEventListener {

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
        GetMyInfoResultWithoutSerializable getMyInfoResult = new GetMyInfoResultWithoutSerializable(myInfo);
        try {
            promise.resolve(getMyInfoResult.getUserProfile2WritableNativeMap());
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
        Enterprise enterprise = ((MyApplication) getReactApplicationContext().getApplicationContext()).getCurrentEnterprise();
        try {
            promise.resolve(enterprise.enterPrise2WritableNativeMap());
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    @ReactMethod
    public void alertDialog(String title, String content, String buttonJson, final Promise promise) {
        CustomDialog.MessageDialogBuilder messageDialogBuilder = new CustomDialog.MessageDialogBuilder(getCurrentActivity());
        if (!StringUtils.isBlank(title)) {
            messageDialogBuilder.setTitle(title);
        }
        if (!StringUtils.isBlank(content)) {
            messageDialogBuilder.setMessage(content);
        }
        JSONArray array = JSONUtils.getJSONArray(buttonJson, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            final AlertButton alertButton = new AlertButton(JSONUtils.getJSONObject(array, i, new JSONObject()));
            messageDialogBuilder.setPositiveButton(alertButton.getText(), (dialog, index) -> {
                dialog.dismiss();
                try {
                    promise.resolve(alertButton.getCode());
                } catch (Exception e) {
                    promise.reject(e);
                }
            });
        }
        messageDialogBuilder.show();

    }

    @ReactMethod
    public void showToast(String content, Promise promise) {
        ToastUtils.show(MyApplication.getInstance(), content, Toast.LENGTH_LONG);
    }


    /**
     * 通讯录选人
     *
     * @param multi   是否多选
     * @param promise
     */
    @ReactMethod
    public void openContactsPicker(Boolean multi, ReadableArray array, Promise promise) {
        List<SearchModel> searchModelList = new ArrayList<>();
        if (array != null) {
            int arraySize = array.size();
            for (int i = 0; i < arraySize; i++) {
                try {
                    SearchModel searchModel = new SearchModel(array.getMap(i));
                    searchModelList.add(searchModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.promise = promise;
        this.multi = multi;
        Intent intent = new Intent();
        intent.setClass(getReactApplicationContext(),
                ContactSearchActivity.class);
        intent.putExtra("select_content", 2);
        intent.putExtra("isMulti_select", multi);
        intent.putExtra("title", getReactApplicationContext().getString(R.string.adress_list));
        intent.putExtra("isContainMe", true);
        if (searchModelList != null && multi) {
            intent.putExtra("hasSearchResult", (Serializable) searchModelList);
        }
        getCurrentActivity().startActivityForResult(intent, CONTACT_PICKER);

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
        if (requestCode == CONTACT_PICKER) {
            if (promise != null) {
                if (resultCode == RESULT_OK) {
                    List<SearchModel> searchModelList = (List<SearchModel>) data.getSerializableExtra("selectMemList");
                    List<String> uidList = new ArrayList<>();
                    for (int i = 0; i < searchModelList.size(); i++) {
                        String contactId = searchModelList.get(i).getId();
                        if (!StringUtils.isBlank(contactId)) {
                            uidList.add(contactId);
                        }
                    }
                    List<ContactUser> contactUserList = ContactUserCacheUtils.getSoreUserList(uidList);
                    if (multi) {
                        WritableNativeArray writableNativeArray = new WritableNativeArray();
                        for (ContactUser contactUser : contactUserList) {
                            WritableNativeMap map = contactUser2Map(contactUser);
                            writableNativeArray.pushMap(map);
                        }
                        promise.resolve(writableNativeArray);
                    } else {
                        ContactUser contactUser = contactUserList.get(0);
                        WritableNativeMap map = contactUser2Map(contactUser);
                        promise.resolve(map);
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    promise.reject(new Exception("picker was cancelled"));
                }
            }

        }

    }

    /**
     * 根据email查找联系人接口
     * 2017/10/24修改
     * 把抛出异常改为返回null
     * 修改原因是当RN传空字符串（如离职人员会产生空字符串）
     * 保证不阻挡继续执行
     *
     * @param email
     * @param promise
     */
    @ReactMethod
    public void findContactByMail(String email, Promise promise) {
        if (StringUtils.isBlank(email) || email.equals("null")) {
            promise.resolve(null);
            return;
        }
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByEmail(email);
        if (contactUser != null) {
            WritableNativeMap map = contactUser2Map(contactUser);
            promise.resolve(map);
        } else {
            promise.resolve(null);
        }
    }

    public WritableNativeMap contactUser2Map(ContactUser contactUser) {
        WritableNativeMap map = new WritableNativeMap();
        try {
            map.putString("inspur_id", contactUser.getId());
            //   map.putString("code", code);
            map.putString("real_name", contactUser.getName());
            map.putString("pinyin", contactUser.getPinyin());
            map.putString("mobile", contactUser.getMobile());
            map.putString("email", contactUser.getEmail());
            //   map.putString("org_name", orgName);
            //  map.putString("type", type);
            map.putString("head", APIUri.getUserIconUrl(MyApplication.getInstance(), contactUser.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
