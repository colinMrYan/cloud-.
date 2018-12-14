package com.inspur.emmcloud.util.common.systool.permission;

import android.content.Context;
import android.text.TextUtils;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Setting;

import java.util.List;

/**
 * 1.请求单个权限或者权限组的时候使用的是Permission这个类下的，如：Permission.WRITE_EXTERNAL_STORAGE
 * 2.AndPermission可以在任何地方使用 如：AndPermission.with(activity/fragment/context)
 * 3.当用户拒绝时想要再次申请调用Rationale方法，在该方法中去申请
 * 4.AndPermission.hasAlwaysDeniedPermission只能在onDenied()的回调中调用，不能在其它地方使用
 */
public class PermissionManagerUtils {

    private static PermissionManagerUtils permissionManagerUtils;

    public static PermissionManagerUtils getInstance(){
        if(permissionManagerUtils == null){
            synchronized (PermissionManagerUtils.class){
                if(permissionManagerUtils == null){
                    permissionManagerUtils = new PermissionManagerUtils();
                }
            }
        }
        return permissionManagerUtils;
    }

    private PermissionManagerUtils(){}

    /**
     * 请求单个权限
     * @param context
     */
    public void requestSinglePermission(Context context,String permission,PermissionRequestCallback callback){
        requestGroupPermission(context,new String[]{permission},callback);
    }

    /**
     * 请求一组权限
     * @param context
     */
    public void requestGroupPermission(final Context context, String[] permissionGroup, final PermissionRequestCallback callback){
        if(callback == null){
            return;
        }
        if(permissionGroup == null || permissionGroup.length == 0){
            callback.onPermissionRequestException(new Exception("permissionGroup is null"));
            return;
        }
        AndPermission.with(context)
                .runtime()
                .permission(permissionGroup)
//                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        LogUtils.YfcDebug("申请成功");
                        callback.onPermissionRequestSuccess(permissions);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        LogUtils.YfcDebug("申请失败");
                        if (AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
                            LogUtils.YfcDebug("永久拒绝，");
                            showSettingDialog(context, permissions);
                        }else{
                            LogUtils.YfcDebug("拒绝一次");
                            callback.onPermissionRequestFail(permissions);
                        }
                    }
                })
                .start();
    }

    /**
     * 展示设置权限dialog
     */
    private void showSettingDialog(final Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.permission_message_always_failed, TextUtils.join("\n", permissionNames));
        new MyQMUIDialog.MessageDialogBuilder(context)
                .setTitle(R.string.permission_dialog_title)
                .setMessage(message)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        LogUtils.YfcDebug("点击取消按钮");
                        MyApplication.getInstance().exit();
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.settings, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        LogUtils.YfcDebug("点击确定按钮");
                        dialog.dismiss();
                        setPermission(context);
                        MyApplication.getInstance().exit();
                    }
                })
                .show();

//        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        //    设置Title的图标
//        builder.setIcon(R.drawable.ic_launcher);
//        //    设置Title的内容
//        builder.setTitle("title");
//        //    设置Content来显示一个信息
//        builder.setMessage(message);
//        //    设置一个PositiveButton
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                setPermission(context);
//                Toast.makeText(context, "positive: " + which, Toast.LENGTH_SHORT).show();
//            }
//        });
//        //    设置一个NegativeButton
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(context, "negative: " + which, Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.show();
    }

    /**
     * 设置权限回来
     */
    private void setPermission(final Context context) {
        AndPermission.with(context)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        //判断申请权限是否成功
                        if(!(PermissionManagerUtils.getInstance().isHasPermission(MyApplication.getInstance(), Permissions.STORAGE)
                                &&PermissionManagerUtils.getInstance().isHasPermission(MyApplication.getInstance(), Permission.READ_PHONE_STATE))){
                            MyApplication.getInstance().exit();
                        }
//                        Toast.makeText(context, R.string.permission_message_setting_comeback, Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }


    /**
     * 检测单个权限
     * @param context
     * @param permission
     * @return
     */
    public boolean isHasPermission(Context context,String permission){
        return AndPermission.hasPermissions(context,permission);
    }

    /**
     * 检查一组权限
     * @param context
     * @param permissions
     * @return
     */
    public boolean isHasPermission(Context context,String[] permissions){
        return AndPermission.hasPermissions(context,permissions);
    }


}
