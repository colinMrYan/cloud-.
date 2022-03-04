package com.inspur.emmcloud.basemodule.util.systool.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.EmmPermission;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.EmmPermissionBase;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.EmmPermissionListener;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PermissionRequestManagerUtils {

    private static PermissionRequestManagerUtils permissionManagerUtils;
    private PermissionRequestCallback callback;
    private Context context;

    private PermissionRequestManagerUtils() {
    }

    public static PermissionRequestManagerUtils getInstance() {
        if (permissionManagerUtils == null) {
            synchronized (PermissionRequestManagerUtils.class) {
                if (permissionManagerUtils == null) {
                    permissionManagerUtils = new PermissionRequestManagerUtils();
                }
            }
        }
        return permissionManagerUtils;
    }

    /**
     * 请求单个权限
     *
     * @param context
     */
    public void requestRuntimePermission(Context context, String permission, PermissionRequestCallback callback) {
        requestRuntimePermission(context, new String[]{permission}, callback);
    }

    /**
     * 请求一组权限
     *
     * @param context
     */
    public void requestRuntimePermission(final Context context, String[] permissionGroup, final PermissionRequestCallback callback) {
        if (callback == null) {
            return;
        }
        this.callback = callback;
        this.context = context;
        if (permissionGroup == null || permissionGroup.length == 0 || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.onPermissionRequestSuccess(new ArrayList<String>());
            return;
        }
        if (PermissionRequestManagerUtils.getInstance().isHasPermission(context, permissionGroup)) {
            callback.onPermissionRequestSuccess(Arrays.asList(permissionGroup));
        } else {
            EmmPermissionListener permissionlistener = new EmmPermissionListener() {
                @Override
                public void onPermissionGranted(List<String> grantPermissions) {
                    if (callback != null) {
                        callback.onPermissionRequestSuccess(grantPermissions);
                    }
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    exitByPermission(deniedPermissions);
                }
            };
            EmmPermission.with(context)
                    .setPermissionListener(permissionlistener)
                    .setGotoSettingButtonText(R.string.ok)
                    .setPermissions(permissionGroup)
                    .check();
        }
    }

    private void exitByPermission(List<String> permissionList) {
        if (!(PermissionRequestManagerUtils.getInstance().isHasPermission(BaseApplication.getInstance(), Permissions.STORAGE)
                && PermissionRequestManagerUtils.getInstance().isHasPermission(BaseApplication.getInstance(), Permissions.READ_PHONE_STATE))) {
            if (callback != null) {
                callback.onPermissionRequestFail(permissionList);
            }
//            BaseApplication.getInstance().exit();
        } else {
            if (isHasPermission(context, stringList2StringArray(permissionList))) {
                if (callback != null) {
                    callback.onPermissionRequestSuccess(permissionList);
                }
            } else {
                if (callback != null) {
                    callback.onPermissionRequestFail(permissionList);
                }
            }
        }
    }

    /**
     * 检测单个权限
     *
     * @param context
     * @param permission
     * @return
     */
    public boolean isHasPermission(Context context, String permission) {
        return EmmPermissionBase.isGranted(context, permission);
    }

    /**
     * 检查一组权限
     *
     * @param context
     * @param permissions
     * @return
     */
    public boolean isHasPermission(Context context, String[] permissions) {
        return EmmPermissionBase.isGranted(context, permissions);
    }

    private String[] stringList2StringArray(List<String> permissionList) {
        String[] strings = new String[permissionList.size()];
        return permissionList.toArray(strings);
    }

    @SuppressLint("StringFormatInvalid")
    public String getPermissionToast(Context context, List<String> permissionList) {
        List<String> permissionNameList = Permissions.transformText(context, permissionList);
        return context.getString(R.string.permission_grant_fail, AppUtils.getAppName(context), TextUtils.join(" ", permissionNameList));
    }


}
