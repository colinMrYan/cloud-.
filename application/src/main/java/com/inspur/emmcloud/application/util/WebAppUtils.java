package com.inspur.emmcloud.application.util;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.GetWebAppRealUrlResult;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.componentservice.communication.OnGetWebAppRealUrlListener;

/**
 * Created by chenmch on 2017/11/1.
 */

public class WebAppUtils {
    private Activity activity;
    private OnGetWebAppRealUrlListener onGetWebAppRealUrlListener;
    private LoadingDialog loadingDlg;

    public WebAppUtils(Activity activity, OnGetWebAppRealUrlListener onGetWebAppRealUrlListener) {
        this.activity = activity;
        this.onGetWebAppRealUrlListener = onGetWebAppRealUrlListener;
        loadingDlg = new LoadingDialog(activity);
    }

    public void getWebAppRealUrl(String url) {
        if (NetUtils.isNetworkConnected(activity, false)) {
            loadingDlg.show();
            ApplicationAPIService apiService = new ApplicationAPIService(activity);
            apiService.setAPIInterface(new WebService());
            apiService.getWebAppRealUrl(url);
        } else if (onGetWebAppRealUrlListener != null) {
            onGetWebAppRealUrlListener.getWebAppRealUrlFail();
        }
    }

    private class WebService extends ApplicationApiInterfaceImpl {
        @Override
        public void returnWebAppRealUrlSuccess(GetWebAppRealUrlResult getWebAppRealUrlResult) {
            String webAppRealUrl = getWebAppRealUrlResult.getUrl();
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (onGetWebAppRealUrlListener != null) {
                onGetWebAppRealUrlListener.getWebAppRealUrlSuccess(webAppRealUrl);
            }
        }

        @Override
        public void returnWebAppRealUrlFail() {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (onGetWebAppRealUrlListener != null) {
                onGetWebAppRealUrlListener.getWebAppRealUrlFail();
            }
        }
    }

}
