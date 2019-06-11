package com.inspur.emmcloud.basemodule.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
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
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.util.List;

public abstract class BaseFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        checkNecessaryPermission();
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

    protected void setTheme() {
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
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

    protected void setStatus() {
        int statusBarColor = ResourceUtils.getResValueOfAttr(this, R.attr.header_bg_color);
        int navigationBarColor = android.R.color.white;
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
}
