package com.inspur.emmcloud.util.common.systool.permission;

import android.app.Activity;

import com.inspur.emmcloud.util.common.LogUtils;

import java.util.List;

/**
 * Created by yufuchang on 2018/10/25.
 */

public class NecessaryPermissionsApplyUtils {

    public void getNecessaryPermissions(Activity activity){
        if(!PermissionManagerUtils.getInstance().isHasPermission(activity, Permissions.STORAGE)){
            LogUtils.YfcDebug("没有sd卡权限，开始申请");
            PermissionManagerUtils.getInstance().requestGroupPermission(activity, Permissions.STORAGE, new PermissionRequestCallback() {
                @Override
                public void onPermissionRequestSuccess(List<String> permissions) {
                    LogUtils.YfcDebug("申请成功");
                }

                @Override
                public void onPermissionRequestFail(List<String> permissions) {
                    LogUtils.YfcDebug("申请失败");
                }

                @Override
                public void onPermissionRequestException(Exception e) {
                    LogUtils.YfcDebug("出现异常："+e.getMessage());
                }
            });
        }
    }
}
