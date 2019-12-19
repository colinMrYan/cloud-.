package com.inspur.emmcloud.basemodule.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private int statusType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        statusType = getStatusType();
        setTheme();
        super.onCreate(savedInstanceState);
        int layoutResId = getLayoutResId();
        if (layoutResId != 0) {
            setContentView(layoutResId);
        }
        checkNecessaryPermission();
        setStatus();
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
            default:
                setTheme(statusType == STATUS_TRANSPARENT ? R.style.AppTheme_Transparent_0 : R.style.AppTheme_0);
                break;
        }
    }

    private void setStatus() {
        int navigationBarColor = android.R.color.white;
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
                ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarColor(android.R.color.white).statusBarDarkFont(true, 0.2f).init();
                break;
            case STATUS_TRANSPARENT:
                ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(isStatusBarDarkFont, 0.2f).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).init();
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
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }
}
