package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.GetWebAppRealUrlResult;

/**
 * Created by chenmch on 2017/11/1.
 */

public class WebAppUtils {
    private Context context;
    private OnGetWebAppRealUrlListener onGetWebAppRealUrlListener;
    private LoadingDialog loadingDlg;

    public WebAppUtils(Context context, OnGetWebAppRealUrlListener onGetWebAppRealUrlListener) {
        this.context = context;
        this.onGetWebAppRealUrlListener = onGetWebAppRealUrlListener;
        loadingDlg = new LoadingDialog(context);
    }

    public void getWebAppRealUrl(String url) {
        if (NetUtils.isNetworkConnected(context, false)) {
            loadingDlg.show();
            MyAppAPIService apiService = new MyAppAPIService(context);
            apiService.setAPIInterface(new WebService());
            apiService.getWebAppRealUrl(url);
        } else if (onGetWebAppRealUrlListener != null) {
            onGetWebAppRealUrlListener.getWebAppRealUrlFail();
        }
    }

    public interface OnGetWebAppRealUrlListener {
        void getWebAppRealUrlSuccess(String webAppUrl);

        void getWebAppRealUrlFail();
    }

    private class WebService extends APIInterfaceInstance {
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
