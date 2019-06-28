package com.inspur.emmcloud.web.ui;

import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.web.R;

@Route(path = "/web/main")
public class ImpActivity extends ImpFragmentBaseActivity {

    public static final int DO_NOTHING_RESULTCODE = 5;
    private ImpFragment fragment;

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isWebAutoRotate = false;
        Router router = Router.getInstance();
        if (router.getService(AppService.class) != null) {
            AppService service = router.getService(AppService.class);
            isWebAutoRotate = Boolean.parseBoolean(service.getAppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "false"));
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

}
