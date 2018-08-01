package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.appcenter.AndroidBundleBean;
import com.inspur.emmcloud.bean.appcenter.ReactNativeUpdateBean;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.reactnative.ReactNativeFlow;

import java.io.File;

/**
 * Created by chenmch on 2017/10/10.
 */

public class ReactNativeUtils {
    private Context context;
    private String uid;
    private String reactNativeCurrentPath;
    public ReactNativeUtils(Context context){
        this.context = context;
        uid = ((MyApplication)context.getApplicationContext()).getUid();
    }

    public void init(){
        reactNativeCurrentPath = MyAppConfig.getReactAppFilePath(context, uid, "discover");
        if (!FileUtils.isFileExist(reactNativeCurrentPath + "/index.android.bundle")) {
            ReactNativeFlow.initReactNative(context, uid);
        } else {

            new ClientIDUtils(MyApplication.getInstance(), new ClientIDUtils.OnGetClientIdListener() {
                @Override
                public void getClientIdSuccess(String clientId) {
                    updateReactNative();
                }

                @Override
                public void getClientIdFail() {
                }
            }).getClientId();
        }
    }

    /**
     * 更新ReactNative
     */
    private void updateReactNative() {
        String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CLIENTID, "");
        StringBuilder describeVersionAndTime = FileUtils.readFile(reactNativeCurrentPath + "/bundle.json", "UTF-8");
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        if (NetUtils.isNetworkConnected(context, false)) {
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(new WebService());
            apiService.getReactNativeUpdate(androidBundleBean.getVersion(), androidBundleBean.getCreationDate(), clientId);
        }
    }

    /**
     * 按照更新指令更新ReactNative
     */
    private void updateReactNativeWithOrder(ReactNativeUpdateBean reactNativeUpdateBean) {
        int state = ReactNativeFlow.checkReactNativeOperation(reactNativeUpdateBean.getCommand());
        String reactNatviveTempPath = MyAppConfig.getReactTempFilePath(context, uid);
        if (state == ReactNativeFlow.REACT_NATIVE_RESET) {
            //删除current和temp目录，重新解压assets下的zip
            resetReactNative();
            FindFragment.hasUpdated = true;
        } else if (state == ReactNativeFlow.REACT_NATIVE_ROLLBACK) {
            //拷贝temp下的current到app内部current目录下
            File file = new File(reactNatviveTempPath);
            if (file.exists()) {
                FileUtils.copyFolder(reactNatviveTempPath, reactNativeCurrentPath);
                LogUtils.YfcDebug("回滚时temp：" + reactNatviveTempPath);
                LogUtils.YfcDebug("回滚时current：" + reactNativeCurrentPath);
                FileUtils.deleteFile(reactNatviveTempPath);
            } else {
                ReactNativeFlow.initReactNative(context, uid);
            }
            FindFragment.hasUpdated = true;
        } else if (state == ReactNativeFlow.REACT_NATIVE_FORWORD) {
            LogUtils.YfcDebug("Forword");
            //下载zip包并检查是否完整，完整则解压，不完整则重新下载,完整则把current移动到temp下，把新包解压到current
            ReactNativeFlow.downLoadZipFile(context, reactNativeUpdateBean, uid);
        } else if (state == ReactNativeFlow.REACT_NATIVE_UNKNOWN) {
            //发生了未知错误，下载state为0
            //同Reset的情况，删除current和temp目录，重新解压assets下的zip
            resetReactNative();
            FindFragment.hasUpdated = true;
        } else if (state == ReactNativeFlow.REACT_NATIVE_NO_UPDATE) {
            //没有更新什么也不做
        }
    }


    /**
     * 重新整理目录恢复状态
     */
    private void resetReactNative() {
        String reactNatviveTempPath = MyAppConfig.getReactTempFilePath(context, uid);
        FileUtils.deleteFile(reactNatviveTempPath);
        FileUtils.deleteFile(reactNativeCurrentPath);
        ReactNativeFlow.initReactNative(context, uid);
    }



    private class WebService extends APIInterfaceInstance{
        @Override
        public void returnReactNativeUpdateSuccess(ReactNativeUpdateBean reactNativeUpdateBean) {
            //保存下返回的ReactNative更新信息，回写日志时需要用
            updateReactNativeWithOrder(reactNativeUpdateBean);
//            PreferencesUtils.putString(context,"react_native_lastupdatetime",System.currentTimeMillis()+"");//隔半小时检查更新逻辑
        }

        @Override
        public void returnReactNativeUpdateFail(String error, int errorCode) {
        }
    }
}
