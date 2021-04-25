package com.inspur.emmcloud.basemodule.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.util.List;

import static com.inspur.emmcloud.basemodule.ui.BaseActivity.THEME_DARK;
import static com.inspur.emmcloud.basemodule.util.protocol.ProtocolUtil.PREF_PROTOCOL_DLG_AGREED;

public abstract class BaseFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        // 设置是否开启原生页面自动旋转
        boolean isNativeAutoRotate = PreferencesByUserAndTanentUtils.getBoolean(this,
                Constant.PREF_APP_OPEN_NATIVE_ROTATE_SWITCH, false);
        if (isNativeAutoRotate) {
            if (this instanceof NotSupportLand) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        if(PreferencesUtils.getBoolean(this, PREF_PROTOCOL_DLG_AGREED, false)){
            checkNecessaryPermission();
        } else {
            //目前仅有可能时隐私H5页
            onCreate();
        }
    }

    private void checkNecessaryPermission() {
        final String[] necessaryPermissionArray =
                StringUtils.concatAll(Permissions.STORAGE, new String[]{Permissions.READ_PHONE_STATE});
        if (!PermissionRequestManagerUtils.getInstance().isHasPermission(this, necessaryPermissionArray)) {
            final MyDialog permissionDialog = new MyDialog(this, R.layout.dialog_permisson_tip);
            permissionDialog.setDimAmount(0.2f);
            permissionDialog.setCancelable(false);
            permissionDialog.setCanceledOnTouchOutside(false);
            permissionDialog.findViewById(R.id.ll_permission_storage).setVisibility(
                    !PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.STORAGE)
                            ? View.VISIBLE
                            : View.GONE);
            permissionDialog.findViewById(R.id.ll_permission_phone).setVisibility(
                    !PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.READ_PHONE_STATE)
                            ? View.VISIBLE
                            : View.GONE);
            if (!PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.STORAGE)
                    && !PermissionRequestManagerUtils.getInstance().isHasPermission(this,
                    Permissions.READ_PHONE_STATE)) {
                LinearLayout layout = permissionDialog.findViewById(R.id.ll_permission_storage);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                params.setMargins(DensityUtil.dip2px(this, 60.0f), 0, 0, 0);
                layout.setLayoutParams(params);
            }
            ((TextView) permissionDialog.findViewById(R.id.tv_permission_dialog_title)).setText(
                    getString(R.string.permission_open_cloud_plus, AppUtils.getAppName(BaseApplication.getInstance())));
            ((TextView) permissionDialog.findViewById(R.id.tv_permission_dialog_summary)).setText(getString(
                    R.string.permission_necessary_permission, AppUtils.getAppName(BaseApplication.getInstance())));
            permissionDialog.findViewById(R.id.tv_next_step).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    permissionDialog.dismiss();
                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(BaseApplication.getInstance(),
                            necessaryPermissionArray, new PermissionRequestCallback() {
                                @Override
                                public void onPermissionRequestSuccess(List<String> permissions) {
                                    onCreate();
                                }

                                @Override
                                public void onPermissionRequestFail(List<String> permissions) {
                                    ToastUtils.show(BaseApplication.getInstance(),
                                            PermissionRequestManagerUtils.getInstance()
                                                    .getPermissionToast(BaseApplication.getInstance(), permissions));
                                    BaseApplication.getInstance().exit();
                                }
                            });
                }
            });
            permissionDialog.show();
        } else {
            onCreate();
        }
    }

    public abstract void onCreate();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
    }

    protected void setTheme() {
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentThemeNo) {
            case 1:
                setTheme(R.style.AppTheme_1);
                break;
            case 2:
                setTheme(R.style.AppTheme_2);
                break;
            case 3:
                setTheme(R.style.AppTheme_3);
                break;
            default:
                setTheme(R.style.AppTheme_0);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.AppTheme_0);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.AppTheme_3);
                break;
        }
    }

    protected void setStatus() {
        int statusBarColor = ResourceUtils.getResValueOfAttr(this, R.attr.header_bg_color);
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        int navigationBarColor = currentThemeNo != THEME_DARK ? android.R.color.white : android.R.color.black;
        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(this, R.attr.status_bar_dark_font);
        ImmersionBar.with(this).statusBarColor(statusBarColor).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarDarkFont(isStatusBarDarkFont, 0.2f).init();
    }

    protected void setNavigationBarColor(int color) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(ContextCompat.getColor(BaseApplication.getInstance(), color));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
                //不是深色模式
                if (currentThemeNo != 3) {
                    PreferencesUtils.putInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 3);
                    setTheme();
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
                }
                break;
        }
    }
}
