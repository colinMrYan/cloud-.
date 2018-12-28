package com.inspur.emmcloud.util.common.systool.permission;

import android.content.Context;

import java.util.Arrays;

/**
 * Created by yufuchang on 2018/12/28.
 */

public class PermissionMangerUtils {

    private Context context;
    private String[] permissionGroup;
    private PermissionRequestCallback callback;

    public PermissionMangerUtils(Context context, String permission, PermissionRequestCallback callback){
        this(context,new String[]{permission},callback);
    }

    public PermissionMangerUtils(Context context, String[] permissionGroup, PermissionRequestCallback callback){
        this.context = context;
        this.permissionGroup = permissionGroup;
        this.callback = callback;
    }

    public void start(){
        if(PermissionRequestManagerUtils.getInstance().isHasPermission(context,permissionGroup)){
            callback.onPermissionRequestSuccess(Arrays.asList(permissionGroup));
        }else{
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(context,permissionGroup,callback);
        }
    }
}
