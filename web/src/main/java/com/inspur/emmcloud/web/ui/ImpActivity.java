package com.inspur.emmcloud.web.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.web.R;
import com.umeng.socialize.UMShareAPI;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;

import java.util.HashMap;

@Route(path = Constant.AROUTER_CLASS_WEB_MAIN)
public class ImpActivity extends ImpFragmentBaseActivity implements NotSupportLand {

    public static final int DO_NOTHING_RESULTCODE = 5;
    private ImpFragment fragment;
    boolean isWebAutoRotate = false;

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isWebAutoRotate = false;
        initX5SDK();
        Router router = Router.getInstance();
        if (router.getService(AppService.class) != null) {
            AppService service = router.getService(AppService.class);
            isWebAutoRotate = Boolean.parseBoolean(service.getAppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "true"));
        }
        // 设置是否开启webview自动旋转
        setRequestedOrientation(isWebAutoRotate ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(Res.getLayoutID("web_activity_imp"));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        fragment = new ImpFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return fragment.onBackKeyDown();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fragment.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestart() {
        Router router = Router.getInstance();
        if (router.getService(AppService.class) != null) {
            AppService service = router.getService(AppService.class);
            isWebAutoRotate = Boolean.parseBoolean(service.getAppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "true"));
        }
        setRequestedOrientation(isWebAutoRotate ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onRestart();
    }

    public ImpFragment getFragment() {
        if (fragment != null) return fragment;
        return null;
    }

    private void initX5SDK(){
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
    }
}
