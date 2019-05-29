package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResourceUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.systool.emmpermission.Permissions;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.LanguageManager;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import java.util.List;

public abstract class BaseActivity extends Activity {
    protected final int STATUS_NORMAL = 1;
    protected final int STATUS_WHITE = 2;
    protected final int STATUS_WHITE_DARK_FONT = 3;
    protected final int STATUS_TRANSPARENT = 4;
    protected final int STATUS_NO_SET = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setTheme();
        super.onCreate(savedInstanceState);
        int layoutResId = getLayoutResId();
        if (layoutResId != 0) {
            setContentView(layoutResId);
        }
        checkNecessaryPermission();
        setStatus(getStatusType());
    }

    private void checkNecessaryPermission() {
        final String[] necessaryPermissionArray =
                StringUtils.concatAll(Permissions.STORAGE, new String[] { Permissions.READ_PHONE_STATE });
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
                    getString(R.string.permission_open_cloud_plus, AppUtils.getAppName(MyApplication.getInstance())));
            ((TextView) permissionDialog.findViewById(R.id.tv_permission_dialog_summary)).setText(getString(
                    R.string.permission_necessary_permission, AppUtils.getAppName(MyApplication.getInstance())));
            permissionDialog.findViewById(R.id.tv_next_step).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    permissionDialog.dismiss();
                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(MyApplication.getInstance(),
                            necessaryPermissionArray, new PermissionRequestCallback() {
                                @Override
                                public void onPermissionRequestSuccess(List<String> permissions) {
                                    onCreate();
                                }

                                @Override
                                public void onPermissionRequestFail(List<String> permissions) {
                                    ToastUtils.show(MyApplication.getInstance(),
                                            PermissionRequestManagerUtils.getInstance()
                                                    .getPermissionToast(MyApplication.getInstance(), permissions));
                                    MyApplication.getInstance().exit();
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
        int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentThemeNo) {
            case 1:
                setTheme(R.style.AppTheme_1);
                break;
            case 2:
                setTheme(R.style.AppTheme_2);
                break;
            default:
                setTheme(R.style.AppTheme_0);
                break;
        }
    }

    private void setStatus(int statusType) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }
}
