package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.net.Uri;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * Created by yufuchang on 2017/11/22.
 */

public class AppId2AppAndOpenAppUtils {
    private Activity activity;
    private String uri = "";
    private OnFinishActivityListener onFinishActivityListener;
    private LoadingDialog loadingDialog;

    public AppId2AppAndOpenAppUtils(Activity activity) {
        this.activity = activity;
        loadingDialog = new LoadingDialog(activity);
    }

    /**
     * 根据id获取app详情
     */
    public void getAppInfoById(Uri uri) {
        String appId = uri.getHost();
        this.uri = (uri == null) ? "" : uri.toString();
        if (NetUtils.isNetworkConnected(activity) && !StringUtils.isBlank(appId)) {
            loadingDialog.show();
            MyAppAPIService apiService = new MyAppAPIService(activity);
            apiService.setAPIInterface(new WebService());
            apiService.getAppInfo(appId);
        } else {
            finishActivty();
        }
    }

    /**
     * 结束Activity
     */
    private void finishActivty() {
        if (onFinishActivityListener != null) {
            onFinishActivityListener.onFinishActivity();
        }
    }

    /**
     * 设置回调函数
     *
     * @param l
     */
    public void setOnFinishActivityListener(OnFinishActivityListener l) {
        this.onFinishActivityListener = l;
    }

    /**
     * 根据app打开app
     *
     * @param app
     */
    private void handleAppAction(App app) {
        if (!StringUtils.isBlank(app.getAppID())) {
            //特殊处理，不走UriUtils.openApp的逻辑，直接打开appUri，appUri是最终打开地址。
            if (app.getIsSSO() == 1) {
                UriUtils.openWebApp(activity, app.getUri(), app);
            } else {
                UriUtils.openApp(activity, app, "application");
            }
        } else {
            ToastUtils.show(R.string.unsupport_type);
        }
    }


    /**
     * 取消dialog
     */
    private void dismissLoadingDialog() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * 结束Activity的接口
     */
    public interface OnFinishActivityListener {
        void onFinishActivity();
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnAppInfoSuccess(App app) {
            dismissLoadingDialog();
            handleAppAction(app);
            finishActivty();
        }

        @Override
        public void returnAppInfoFail(String error, int errorCode) {
            dismissLoadingDialog();
            WebServiceMiddleUtils.hand(activity, error, errorCode);
            finishActivty();
        }
    }
}
