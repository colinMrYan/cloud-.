package com.inspur.imp.plugin.emm;

import android.os.Build;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.login.login.LoginService;
import com.inspur.imp.plugin.ImpPlugin;
import com.luojilab.component.componentlib.router.Router;

import org.json.JSONObject;


/**
 * 应用相关本地功能类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class EMMService extends ImpPlugin {

    @Override
    public void execute(String action, JSONObject paramsObject) {
        LogUtils.jasonDebug("action=" + action);
        if ("returnEMMstate".equals(action)) {
            returnEMMstate(paramsObject);
        } else if ("webviewReload".equals(action)) {
            webviewReload();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        LogUtils.jasonDebug("action=" + action);
        if ("getDeviceInfo".equals(action)) {
            return getDeviceInfo();
        } else {
            showCallIMPMethodErrorDlg();
        }
        return "";
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
    }


    /**
     * 获取EMM设备信息
     *
     * @return
     */
    private String getDeviceInfo() {
        JSONObject obj = new JSONObject();
        try {
            int deviceType = AppUtils.isTablet(getFragmentContext()) ? 2 : 1;
            obj.put("device_type", deviceType);
            obj.put("device_model", Build.MODEL);
            obj.put("os", "Android");
            obj.put("os_version", Build.VERSION.RELEASE);
            obj.put("resolution", ResolutionUtils.getResolution(getActivity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    private void webviewReload() {
        webview.reload();
    }


    /**
     * 返回设备检查状态
     *
     * @param paramsObject
     */
    private void returnEMMstate(JSONObject paramsObject) {
        try {
            if (paramsObject.has("EMMState")) {
                String state = paramsObject.getString("EMMState");
                String userName = PreferencesUtils.getString(getFragmentContext(), "userRealName", "");
                String userCode = PreferencesUtils.getString(getFragmentContext(), "userID", "");
                Router router = Router.getInstance();
                if (router.getService(LoginService.class.getSimpleName()) != null) {
                    LoginService service = (LoginService) router.getService(LoginService.class.getSimpleName());
                    service.MDMCheck(getActivity(), MyApplication.getInstance().getTanent(), userCode,
                            userName, state, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show(getFragmentContext(), "设备检查信息异常");
        }

    }
}
