package com.inspur.emmcloud.util.common.systool.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.systool.emmpermission.PermissionListener;
import com.inspur.emmcloud.util.common.systool.emmpermission.TedPermission;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.yanzhenjie.permission.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 1.请求单个权限或者权限组的时候使用的是Permission这个类下的，如：Permission.WRITE_EXTERNAL_STORAGE
 * 2.AndPermission可以在任何地方使用 如：AndPermission.with(activity/fragment/context)
 * 3.AndPermission.hasAlwaysDeniedPermission只能在onDenied()的回调中调用，不能在其它地方使用
 */
public class PermissionRequestManagerUtils {

    private static PermissionRequestManagerUtils permissionManagerUtils;
    private PermissionRequestCallback callback;
    private Context context;
    public static final int REQ_CODE_PERMISSION_REQUEST = 10;
    /**
     * Classic permission checker.
     */
    private static final PermissionChecker permissionChecker = new StandardChecker();

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
//            AndPermission.with(context)
//                    .runtime()
//                    .permission(permissionGroup)
//                    .onGranted(new Action<List<String>>() {
//                        @Override
//                        public void onAction(List<String> permissions) {
//                            if (callback != null) {
//                                callback.onPermissionRequestSuccess(permissions);
//                            }
//                        }
//                    })
//                    .onDenied(new Action<List<String>>() {
//                        @Override
//                        public void onAction(List<String> permissions) {
//                            if (AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
//                                showSettingDialog(context, permissions);
//                            } else {
//                                if (callback != null) {
//                                    callback.onPermissionRequestFail(permissions);
//                                }
//                            }
//                        }
//                    })
//                    .start();
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted(List<String> grantPermissions) {
                    if (callback != null) {
                        callback.onPermissionRequestSuccess(grantPermissions);
                    }
//                    Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    exitByPermission(deniedPermissions);
//                    Toast.makeText(context, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT)
//                            .show();
                }
            };


            TedPermission.with(context)
                    .setPermissionListener(permissionlistener)
//                    .setDeniedTitle("Permission denied")
//                    .setDeniedMessage(
//                            "If you reject permission,you can not use this service\nPlease turn on permissions at [Setting] > [Permission]")
                    .setGotoSettingButtonText(R.string.ok)
                    .setPermissions(permissionGroup)
                    .check();
        }
    }

//    /**
//     * 展示设置权限dialog
//     */
//    @SuppressLint("StringFormatMatches")
//    private void showSettingDialog(final Context context, final List<String> permissionList) {
//        if (context instanceof Activity) {
//            List<String> permissionNameList = Permission.transformText(context, permissionList);
//            String message = context.getString(R.string.permission_message_always_failed, AppUtils.getAppName(context), TextUtils.join(" ", permissionNameList));
//            new MyQMUIDialog.MessageDialogBuilder(context)
//                    .setMessage(message)
//                    .addAction(R.string.cancel , new QMUIDialogAction.ActionListener() {
//                        @Override
//                        public void onClick(QMUIDialog dialog, int index) {
//                            dialog.dismiss();
//                            exitByPermission(permissionList);
//                        }
//                    })
//                    .addAction(R.string.settings, new QMUIDialogAction.ActionListener() {
//                        @Override
//                        public void onClick(QMUIDialog dialog, int index) {
//                            dialog.dismiss();
//                            setComeBackFromSysPermissionSetting(context, permissionList);
//                        }
//                    })
//                    .show();
//        } else {
//            if (callback != null) {
//                callback.onPermissionRequestFail(permissionList);
//            }
//        }
//    }

    private void exitByPermission(List<String> permissionList) {
        if (!(PermissionRequestManagerUtils.getInstance().isHasPermission(MyApplication.getInstance(), Permissions.STORAGE)
                && PermissionRequestManagerUtils.getInstance().isHasPermission(MyApplication.getInstance(), Permission.READ_PHONE_STATE))) {
            if (callback != null) {
                callback.onPermissionRequestFail(permissionList);
            }
            MyApplication.getInstance().exit();
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

//    /**
//     * 设置权限回来
//     */
//    private void setComeBackFromSysPermissionSetting(final Context context, final List<String> permissionList) {
//        AndPermission.with(context)
//                .runtime()
//                .setting()
//                .onComeback(new Setting.Action() {
//                    @Override
//                    public void onAction() {
//                        //从设置页面回来，判断申请权限是否成功
//                        exitByPermission(permissionList);
//                    }
//                })
//                .start();
//    }


    /**
     * 检测单个权限
     *
     * @param context
     * @param permission
     * @return
     */
    public boolean isHasPermission(Context context, String permission) {
        return permissionChecker.hasPermission(context,permission);
    }

    /**
     * 检查一组权限
     *
     * @param context
     * @param permissions
     * @return
     */
    public boolean isHasPermission(Context context, String[] permissions) {
        return permissionChecker.hasPermission(context,permissions);
    }

    private String[] stringList2StringArray(List<String> permissionList) {
        String[] strings = new String[permissionList.size()];
        return permissionList.toArray(strings);
    }

    @SuppressLint("StringFormatInvalid")
    public String getPermissionToast(Context context, List<String> permissionList) {
        List<String> permissionNameList = Permission.transformText(context, permissionList);
        return context.getString(R.string.permission_grant_fail, AppUtils.getAppName(context), TextUtils.join(" ", permissionNameList));
    }

}
