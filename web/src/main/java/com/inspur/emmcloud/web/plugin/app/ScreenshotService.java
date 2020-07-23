package com.inspur.emmcloud.web.plugin.app;

import android.view.WindowManager;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by: yufuchang
 * Date: 2020/7/21
 */
public class ScreenshotService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("enableScreenshot".equals(action)) {
            setWindowSecure(false);
        } else if("disableScreenshot".equals(action)){
            setWindowSecure(true);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        if ("enableScreenshot".equals(action)) {
            setWindowSecure(false);
        } else if("disableScreenshot".equals(action)){
            setWindowSecure(true);
        } else {
            showCallIMPMethodErrorDlg();
        }
        return "";
    }

    /**
     * 动态设置防截屏
     * @param isSecure
     */
    private void setWindowSecure(boolean isSecure) {
        if (isSecure) {
            if ((getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                LogUtils.YfcDebug( "flag already set secure");
                return;
            }
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            if ((getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SECURE) == 0) {
                LogUtils.YfcDebug(  "flag already set unsecure");
                return;
            }
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public void onDestroy() {

    }
}
