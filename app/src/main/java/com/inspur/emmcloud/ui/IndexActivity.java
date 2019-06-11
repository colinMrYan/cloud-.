package com.inspur.emmcloud.ui;

import android.os.Build;
import android.os.Handler;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.NotificationSetUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.MyAppWidgetUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 主页面
 *
 * @author Administrator
 */
@Route(path = "/app/index")
public class IndexActivity extends IndexBaseActivity {
    private static final int SYNC_ALL_BASE_DATA_SUCCESS = 0;
    private static final int RELOAD_WEB = 3;
    private Handler handler;
    private boolean isHasCacheContact = false;
    private LoadingDialog loadingDlg;

    @Override
    public void onCreate() {
        super.onCreate();
        initAppEnvironment();
        initView();
        getInitData();
        EventBus.getDefault().register(this);
    }

//    private void getNaviTabData(String naviTabSaveConfigVersion) {
//        if (NetUtils.isNetworkConnected(this, false)) {
//            AppAPIService appAPIService = new AppAPIService(this);
//            appAPIService.setAPIInterface(new WebService());
//            appAPIService.getAppNaviTabs(naviTabSaveConfigVersion);
//        }
//    }

    /**
     * 初始化app的运行环境
     */
    private void initAppEnvironment() {
        MyApplication.getInstance().setIsContactReady(false);
        MyApplication.getInstance().setIndexActvityRunning(true);
        MyApplication.getInstance().restartAllDb();
        MyApplication.getInstance().clearUserPhotoMap();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (NotificationSetUtils.isNotificationEnabled(this) &&
                    (PreferencesByUserAndTanentUtils.putBoolean(IndexActivity.this, Constant.PUSH_SWITCH_FLAG, true))) {
                PushManagerUtils.getInstance().startPush();
            }
        } else {
            if (PreferencesByUserAndTanentUtils.putBoolean(IndexActivity.this, Constant.PUSH_SWITCH_FLAG, true)) {
                PushManagerUtils.getInstance().startPush();
            }
        }

    }

    private void initView() {
        loadingDlg = new LoadingDialog(IndexActivity.this, getString(R.string.app_init));
    }

    /**
     * 初始化
     */
    private void getInitData() {
        PushManagerUtils.getInstance().registerPushId2Emm();
        getMyAppRecommendWidgets();
    }

    /**
     * 获取我的应用推荐小部件数据,如果到了更新时间才请求
     */
    private void getMyAppRecommendWidgets() {
        if (MyAppWidgetUtils.checkNeedUpdateMyAppWidget(IndexActivity.this)) {
            MyAppWidgetUtils.getInstance(getApplicationContext()).getMyAppWidgetsFromNet();
        }
    }


    @Override
    protected void onDestroy() {
        MyApplication.getInstance().setIndexActvityRunning(false);
        if (handler != null) {
            handler = null;
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }









}
