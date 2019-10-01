package com.inspur.emmcloud.web.plugin.invoice;

import android.util.Log;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.api.WebAPIUri;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.tencent.mm.opensdk.modelbiz.ChooseCardFromWXCardPackage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class InvoiceService extends ImpPlugin {
    private String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");

        if (action.equals("invoice")) {
            initInvoice();
        }
    }

    String wechatAccessToken, wechatTicket, timestamp;

    /**
     * 获取sha1值
     *
     * @param info
     * @return
     */
    public static String encryptToSHA(String info) {
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-1");
            alga.update(info.getBytes());
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String rs = byte2hex(digesta);
        return rs;
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    /**
     * 字符串  字典序排列
     *
     * @param list
     * @return
     */
    private String getOriginSignData(List<String> list) {
        Set<String> set = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        set.addAll(list);

        List<String> resultList = new ArrayList<>(set);

        StringBuilder sb = new StringBuilder();
        for (String item : resultList) {
            sb.append(item);
        }

        return sb.toString();
    }

    /**
     * 获取微信的accessToken
     */
    private void initInvoice() {
        IWXAPI api = WXAPIFactory.createWXAPI(getFragmentContext(), Constant.WECHAT_APPID, false);
        if (!api.isWXAppInstalled()) {
            ToastUtils.show(R.string.volume_please_install_wechat);
            return;
        }
        String completeUrl = WebAPIUri.getWechatTicketUrl();
        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(getFragmentContext(), CloudHttpMethod.GET, params, new BaseModuleAPICallback(getFragmentContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                Log.d("zhang", "callbackSuccess: ");
                wechatAccessToken = object.optString("access_token");
                wechatTicket = object.optString("ticket");
                timestamp = object.optString("create_date");
                PreferencesUtils.putString(getFragmentContext(), "WechatAccessToken", wechatAccessToken);
                Log.d("zhang", "callbackSuccess: wechatAccessToken = " + wechatAccessToken +
                        ",wechatTicket = " + wechatTicket);
//                getTicket(wechatAccessToken);
                skipInvoiceList();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                Log.d("zhang", "callbackFail: ");
            }

            @Override
            public void callbackTokenExpire(long requestTime) {

            }
        });
    }

    /**
     * 拉起微信电子发票列表
     */
    private void skipInvoiceList() {
        //注册app
        IWXAPI api = WXAPIFactory.createWXAPI(getFragmentContext(), Constant.WECHAT_APPID, false);

        List<String> list = new ArrayList();
        list.add("INVOICE");
        list.add(Constant.WECHAT_APPID);
        list.add(timestamp);
        list.add("abc");
        list.add(wechatTicket);
        String result = getOriginSignData(list);

        try {
            String sha1 = encryptToSHA(result);
            Log.d("zhang", "initInvoice: sha1=" + sha1);

            //拉起微信电子发票列表
            ChooseCardFromWXCardPackage.Req req = new ChooseCardFromWXCardPackage.Req();
            req.appId = Constant.WECHAT_APPID;
            req.cardType = "INVOICE";
            req.cardSign = sha1;
            req.nonceStr = "abc";
            req.timeStamp = timestamp;
            req.signType = "SHA1";

            if (req.checkArgs()) {
                api.sendReq(req);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据微信的accessToken获取ticket
     *
     * @param wechatAccessToken
     */
    private void getTicket(String wechatAccessToken) {
        String completeUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + wechatAccessToken + "&type=wx_card";

        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(getFragmentContext(), CloudHttpMethod.GET, params, new BaseModuleAPICallback(getFragmentContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                wechatTicket = object.optString("ticket");
                long timeStamp = System.currentTimeMillis();
                Log.d("zhang", "callbackSuccess: ");

            }

            @Override
            public void callbackFail(String error, int responseCode) {
                Log.d("zhang", "callbackFail: ");
            }

            @Override
            public void callbackTokenExpire(long requestTime) {

            }
        });
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        return null;
    }

    @Override
    public void onDestroy() {

    }

    public void handleWechatResult(String result) {
        if (!StringUtils.isBlank(result)) {
            JSONObject json = JSONUtils.getJSONObject(result);
            jsCallback(successCb, json);
        } else {
            try {
                JSONObject json = new JSONObject();
                json.put("errorMessage", getFragmentContext().getString(R.string.unknown_error));
                jsCallback(failCb, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
