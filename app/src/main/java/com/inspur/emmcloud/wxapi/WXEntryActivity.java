package com.inspur.emmcloud.wxapi;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.bean.WechatCardBean;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

public class WXEntryActivity extends WXCallbackActivity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        if (intent == null) return;

        Log.d("zhang", "onCreate: ");
        int type = intent.getIntExtra("_wxapi_command_type", 0);
        if (type == 0) return;

        ArrayList<Parcelable> list = intent.getParcelableArrayListExtra("_wxapi_choose_card_from_wx_card_list");
        String list1 = (String) intent.getSerializableExtra("_wxapi_choose_card_from_wx_card_list");

        try {
            JSONArray array = new JSONArray(list1);
            JSONArray newArray = new JSONArray();

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                WechatCardBean bean = new WechatCardBean();
                bean.card_id = object.optString("card_id");
                bean.encrypt_code = object.optString("encrypt_code");

                JSONObject newObject = new JSONObject();
                newObject.put("card_id", bean.card_id);
                newObject.put("encrypt_code", bean.encrypt_code);
                newArray.put(newObject);
            }

            getInvoiceInfo(newArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("zhang", "onCreate: cardId = ");
    }

    /**
     * 批量查询发票详情
     *
     * @param jsonArray
     */
    private void getInvoiceInfo(JSONArray jsonArray) {
        String wechatAccessToken = PreferencesUtils.getString(this, "WechatAccessToken");
//        "25_z_lDEf8toYld3uS3hRpfMD6FJUXvCoo_RgrOzAha8BmzUtoqeAxnp5chDHSITRa0k8Q_QSOLNUWlU2WE5JBtGNW4ijG46BHiUVRbLEgGUQMLvAiPRGwJ0oNh-WEileazn3f9OTBpPZjD6Up4QJYhAGAEXM";
        String completeUrl = "https://api.weixin.qq.com/card/invoice/reimburse/getinvoicebatch?access_token=" + wechatAccessToken;

        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject json = new JSONObject();
            json.put("item_list", jsonArray);
            params.setBodyContent(json.toString());
            params.setAsJsonContent(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpUtils.request(this, CloudHttpMethod.POST, params, new BaseModuleAPICallback(this, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                Log.d("zhang", "getInvoiceInfo callbackSuccess: ");
                String result = new String(arg0);
                SimpleEventMessage message = new SimpleEventMessage(Constant.EVENTBUS_TAG_WECHAT_RESULT, result);
                org.greenrobot.eventbus.EventBus.getDefault().post(message);
                finish();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                Log.d("zhang", "callbackFail: ");
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                Log.d("zhang", "callbackTokenExpire: ");
            }
        });

    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d("zhang", "onReq: ");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d("zhang", "onResp: ");
    }
}
