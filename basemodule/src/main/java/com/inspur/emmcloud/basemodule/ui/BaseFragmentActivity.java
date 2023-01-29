package com.inspur.emmcloud.basemodule.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;

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
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.util.List;

import static com.inspur.emmcloud.basemodule.ui.BaseActivity.THEME_DARK;

public abstract class BaseFragmentActivity extends FragmentActivity {

    private static boolean checkedNecessaryPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置是否开启原生页面自动旋转
        boolean isNativeAutoRotate = PreferencesUtils.getBoolean(this,
                Constant.PREF_APP_OPEN_NATIVE_ROTATE_SWITCH, false);
        if (isNativeAutoRotate && !(this instanceof NotSupportLand)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setTheme();
        super.onCreate(savedInstanceState);
        onCreate();
//        if (PreferencesUtils.getBoolean(this, PREF_PROTOCOL_DLG_AGREED, false)) {
//            checkNecessaryPermission();
//        } else {
//            //目前仅有可能时隐私H5页
//            onCreate();
//        }
    }

    private void checkNecessaryPermission() {
        checkedNecessaryPermission = true;
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
        float fontScale = PreferencesUtils.getFloat(newBase, Constant.CARING_SWITCH_FLAG, 1);
        if (0 != Float.compare(1.0f, fontScale)) {
            Configuration config = newBase.getResources().getConfiguration();
            config.fontScale = (this instanceof IIgnoreFontScaleActivity) && !((IIgnoreFontScaleActivity) this).followSystemScale() ? 1.0f : fontScale;
            newBase = newBase.createConfigurationContext(config);
        }
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
        // 是否跟随系统改变主题模式
        Boolean followSystem = PreferencesUtils.getBoolean(this, Constant.PREF_FOLLOW_SYSTEM_THEME, true);
        if (!followSystem) {
            return;
        }
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // 系统是浅色模式下，如果当前主题是暗夜黑则改为白色主题，其他主题则保持不变
                if (currentThemeNo == Constant.APP_THEME_DARK) {
                    PreferencesUtils.putInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
                    setTheme();
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
                }
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // 系统是深色模式下，如果当前主题不是暗夜黑则改为暗夜黑主题。
                if (currentThemeNo != Constant.APP_THEME_DARK) {
                    PreferencesUtils.putInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, Constant.APP_THEME_DARK);
                    setTheme();
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
                }
                break;
        }
    }
}
