package com.inspur.emmcloud.web.plugin.telephone;

import android.content.Intent;
import android.net.Uri;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 拨打电话服务
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class TelephoneService extends ImpPlugin {

    private String tel;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // 打开手机拨号界面
        if ("dial".equals(action)) {
            dial(paramsObject);
        }
        // 直接拨打电话
        else if ("call".equals(action)) {
            call(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 跳转到拨号界面
     *
     * @param paramsObject
     */
    private void dial(JSONObject paramsObject) {
        // 解析json串获取到传递过来的参数和回调函数
        try {
            if (!paramsObject.isNull("tel"))
                tel = paramsObject.getString("tel");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!StrUtil.strIsNotNull(tel)) {
            ToastUtils.show(getFragmentContext(), "电话号码不能为空！");
            return;
        }
        Intent intent = new Intent("android.intent.action.DIAL",
                Uri.parse("tel:" + tel));
        getActivity().startActivity(intent);
    }

    /**
     * 直接拨打电话
     *
     * @param paramsObject
     */
    private void call(JSONObject paramsObject) {
        // 解析json串获取到传递过来的参数和回调函数
        try {
            if (!paramsObject.isNull("tel"))
                tel = paramsObject.getString("tel");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!StrUtil.strIsNotNull(tel)) {
            ToastUtils.show(getFragmentContext(), "电话号码不能为空！");
            return;
        }
        AppUtils.call(getActivity(), tel, 1);
    }

    @Override
    public void onDestroy() {

    }
}
