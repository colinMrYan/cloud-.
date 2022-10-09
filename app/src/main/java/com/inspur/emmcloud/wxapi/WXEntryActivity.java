package com.inspur.emmcloud.wxapi;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.bean.WechatCardBean;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;

public class WXEntryActivity extends WXCallbackActivity implements IWXAPIEventHandler {

    private IWXAPI api;
    private MyHandler handler;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        api = WXAPIFactory.createWXAPI(this, Constant.WECHAT_APPID, false);
        handler = new MyHandler(this);

        try {
            Intent intent = getIntent();
            api.handleIntent(intent, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("zhang", "onCreate: ");
    }

    /**
     * 批量查询发票详情
     *
     * @param jsonArray
     */
    private void getInvoiceInfo(JSONArray jsonArray) {
        String wechatAccessToken = PreferencesUtils.getString(this, "WechatAccessToken");
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
            finish();
        }

        HttpUtils.request(this, CloudHttpMethod.POST, params, new BaseModuleAPICallback(this, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                String result = new String(arg0);
                SimpleEventMessage message = new SimpleEventMessage(Constant.EVENTBUS_TAG_WECHAT_RESULT, result);
                org.greenrobot.eventbus.EventBus.getDefault().post(message);
                finish();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                Log.d("zhang", "callbackFail: ");
                finish();
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                Log.d("zhang", "callbackTokenExpire: ");
            }
        });

    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d("zhang", "onReq: type" + baseReq.getType());
        finish();
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d("zhang", "onResp: errCode = " + resp.errCode);
        Log.d("zhang", "onResp: type = " + resp.getType());
        if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
            switch (resp.getType()) {
                case ConstantsAPI.COMMAND_CHOOSE_CARD_FROM_EX_CARD_PACKAGE: //电子发票
                    handleInvoiceResp(resp);
                    break;
//                    case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:    //无异常
//                        SendMessageToWX.Resp msgToWXResp = (SendMessageToWX.Resp)resp;
//                        Log.d("zhang", "onResp: ");
//                        finish();
//                        break;
                default:
                    break;
            }
        } else {
            finish();
        }
    }

    private void handleInvoiceResp(BaseResp resp) {
        ChooseCardFromWXCardPackage.Resp cardResp = (ChooseCardFromWXCardPackage.Resp) resp;
        String cardItemList = cardResp.cardItemList;
        if (StringUtils.isBlank(cardItemList)) {
            finish();
            return;
        }
        try {
            JSONArray array = new JSONArray(cardItemList);
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
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("zhang", "onDestroy: ");
        super.onDestroy();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<WXEntryActivity> wxEntryActivityWeakReference;

        public MyHandler(WXEntryActivity wxEntryActivity) {
            wxEntryActivityWeakReference = new WeakReference<WXEntryActivity>(wxEntryActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            int tag = msg.what;
        }
    }
}
