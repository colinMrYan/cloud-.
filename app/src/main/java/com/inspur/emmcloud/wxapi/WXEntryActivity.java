package com.inspur.emmcloud.wxapi;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.WechatCardBean;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

public class WXEntryActivity extends WXCallbackActivity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        if (intent == null) return;
        String tmp = "nonce_str  code  api_ticket  card_id ";

        Log.d("zhang", "onCreate: ");
        int type = intent.getIntExtra("_wxapi_command_type", 0);
        if (type == 0) return;

        ArrayList<Parcelable> list = intent.getParcelableArrayListExtra("_wxapi_choose_card_from_wx_card_list");
        String list1 = (String) intent.getSerializableExtra("_wxapi_choose_card_from_wx_card_list");

        List<WechatCardBean> cardBeans = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(list1);
            cardBeans.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                WechatCardBean bean = new WechatCardBean();
                bean.card_id = object.optString("card_id");
                bean.encrypt_code = object.optString("encrypt_code");
                cardBeans.add(bean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (cardBeans.size() > 0) {
            getInvoiceInfo(cardBeans.get(0));
        }

        Log.d("zhang", "onCreate: cardId = ");

    }

    /**
     * 获取单个发票详情
     *
     * @param wechatCardBean
     */
    private void getInvoiceInfo(WechatCardBean wechatCardBean) {
        String wechatAccessToken = "25_z_lDEf8toYld3uS3hRpfMD6FJUXvCoo_RgrOzAha8BmzUtoqeAxnp5chDHSITRa0k8Q_QSOLNUWlU2WE5JBtGNW4ijG46BHiUVRbLEgGUQMLvAiPRGwJ0oNh-WEileazn3f9OTBpPZjD6Up4QJYhAGAEXM";
        String completeUrl = "https://api.weixin.qq.com/card/invoice/reimburse/getinvoiceinfo?access_token=" + wechatAccessToken;

        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject json = new JSONObject();
            json.put("card_id", wechatCardBean.card_id);
            json.put("encrypt_code", wechatCardBean.encrypt_code);
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
            }

            @Override
            public void callbackFail(String error, int responseCode) {

            }

            @Override
            public void callbackTokenExpire(long requestTime) {

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
