package com.inspur.emmcloud.basemodule.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;
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
import com.inspur.emmcloud.basemodule.util.protocol.ProtocolUtil;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
    protected final int STATUS_NORMAL = 1;
    protected final int STATUS_WHITE = 2;
    protected final int STATUS_WHITE_DARK_FONT = 3;
    protected final int STATUS_TRANSPARENT = 4;
    protected final int STATUS_NO_SET = 5;
    protected final int STATUS_FULL_SCREEN = 6;
    public static final int THEME_DARK = 3;
    private int statusType;

    private static boolean checkedNecessaryPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        statusType = getStatusType();
        // 8.0 activity透明时，不能设置setRequestedOrientation，目前只有AppSchemeHandleActivity，ShareToConversationBlankActivity
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && statusType == STATUS_TRANSPARENT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            // 设置是否开启原生页面自动旋转
            boolean isNativeAutoRotate = PreferencesUtils.getBoolean(this,
                    Constant.PREF_APP_OPEN_NATIVE_ROTATE_SWITCH, false);
            if (isNativeAutoRotate && !(this instanceof NotSupportLand)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setTheme();

        super.onCreate(savedInstanceState);

        int layoutResId = getLayoutResId();
        if (layoutResId != 0) {
            setContentView(layoutResId);
        }
        setStatus();
        if (checkedNecessaryPermission) {
            onCreate();
            return;
        }
        //首页先同意隐私协议再检查权限
        if (this instanceof IMainActivity) {
            ProtocolUtil.showProtocolDialog(this, new ProtocolUtil.ProtocolDialogCallback() {
                @Override
                public void onAgreed() {
                    ((IMainActivity) BaseActivity.this).onAgreedProtocol();
                    checkNecessaryPermission();
                }

                @Override
                public void onClickAgreeButton() {
                    ((IMainActivity) BaseActivity.this).onClickedProtocolAgreedButton();
                    checkNecessaryPermission();
                }
            });
        } else {
            //避免非主页调用不到onCreate
            onCreate();
        }

//        else {
//            checkNecessaryPermission();
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
                                    onCreate();
//                                    BaseApplication.getInstance().exit();
                                }
                            });
                }
            });
            permissionDialog.show();
        } else {
            onCreate();
        }
    }

    /**
     * 打开
     */
    public abstract void onCreate();

    public abstract int getLayoutResId();

    protected int getStatusType() {
        return STATUS_NORMAL;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        float fontScale = PreferencesUtils.getFloat(newBase, Constant.CARING_SWITCH_FLAG, 1);
        if (0 != Float.compare(1.0f, fontScale)) {
            Configuration config = newBase.getResources().getConfiguration();
            config.fontScale = this instanceof IIgnoreFontScaleActivity ? 1.0f : fontScale;
            newBase = newBase.createConfigurationContext(config);
        }
        super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
    }

    public void setTheme() {
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentThemeNo) {
            case 1:
                setTheme(statusType == STATUS_TRANSPARENT ? R.style.AppTheme_Transparent_1 : R.style.AppTheme_1);
                break;
            case 2:
                setTheme(statusType == STATUS_TRANSPARENT ? R.style.AppTheme_Transparent_2 : R.style.AppTheme_2);
                break;
            case 3:
                setTheme(statusType == STATUS_TRANSPARENT ? R.style.AppTheme_Transparent_3 : R.style.AppTheme_3);
                break;
            default:
                setTheme(statusType == STATUS_TRANSPARENT ? R.style.AppTheme_Transparent_0 : R.style.AppTheme_0);
                break;
        }
    }

    private void setStatus() {
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        int navigationBarColor = currentThemeNo != THEME_DARK ? android.R.color.white : android.R.color.black;
        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(this, R.attr.status_bar_dark_font);
        switch (statusType) {
            case STATUS_NORMAL:
                int statusBarColor = ResourceUtils.getResValueOfAttr(BaseActivity.this, R.attr.header_bg_color);
                ImmersionBar.with(this).statusBarColor(statusBarColor).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarDarkFont(isStatusBarDarkFont, 0.2f).init();
                break;
            case STATUS_WHITE:
                ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).init();
                break;
            case STATUS_WHITE_DARK_FONT:
                if (currentThemeNo != THEME_DARK) {
                    ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarColor(android.R.color.white).statusBarDarkFont(true, 0.2f).init();
                } else {
                    int statusBarColorDark = ResourceUtils.getResValueOfAttr(BaseActivity.this, R.attr.header_bg_color);
                    ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(false, 1.0f).statusBarColor(statusBarColorDark).statusBarDarkFont(false, 0.2f).init();
                }
                break;
            case STATUS_TRANSPARENT:
                ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(isStatusBarDarkFont, 0.2f).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).init();
                break;
            case STATUS_FULL_SCREEN:
                ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarDarkFont(isStatusBarDarkFont, 0.2f).init();
                break;
            default:
                break;
        }

    }

    public void setTitleText(int resId) {
        setTitleText(getResources().getString(resId));
    }

    public void setTitleText(String title) {
        TextView titleTv = findViewById(R.id.header_text);
        if (titleTv != null) {
            titleTv.setVisibility(View.VISIBLE);
            titleTv.setText(title);
        }
    }

    @Override
    protected void onResume() {
        LogUtils.debug("TilllLog", this + " onResume");
        super.onResume();
    }


    @Override
    protected void onPause() {
        LogUtils.debug("TilllLog", this + " onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 启动页和splash页不适配深色模式
        Class<? extends BaseActivity> aClass = getClass();
        String simpleName = aClass.getSimpleName();
        if (simpleName.equals("MainActivity")) {
            return;
        }
        // 第一次打开app,splash页面不适配深色模式
        boolean isFirst = PreferencesUtils.getBoolean(this, "isFirst", true);
        if (isFirst) {
            return;
        }
        // 是否跟随系统改变主题模式
        boolean followSystem = PreferencesUtils.getBoolean(this, Constant.PREF_FOLLOW_SYSTEM_THEME, true);
        if (!followSystem) {
            return;
        }
        followSystemTheme();
    }

    // 跟随系统改变主题
    protected void followSystemTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // 系统是浅色模式下，如果当前主题是暗夜黑则改为白色主题，其他主题则保持不变
                if (currentThemeNo == THEME_DARK) {
                    PreferencesUtils.putInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
                    setTheme();
                    // 登录相关页面不能跳转IndexActivity
                    if (TextUtils.isEmpty(PreferencesUtils.getString(BaseApplication.getInstance(), "userID"))) {
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_MAIN).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
                    } else {
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
                    }
                }
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // 系统是深色模式下，如果当前主题不是暗夜黑则改为暗夜黑主题。
                if (currentThemeNo != THEME_DARK) {
                    PreferencesUtils.putInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, THEME_DARK);
                    setTheme();
                    // 登录相关页面不能跳转IndexActivity
                    if (TextUtils.isEmpty(PreferencesUtils.getString(BaseApplication.getInstance(), "userID"))) {
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_MAIN).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
                    } else {
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_INDEX).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
                    }
                }
                break;
        }
    }
}
