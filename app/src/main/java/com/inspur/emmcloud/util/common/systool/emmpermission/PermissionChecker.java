package com.inspur.emmcloud.util.common.systool.emmpermission;



import android.content.Context;

import java.util.List;

/**
 * Created by yufuchang on 2019/3/20.
 */
public interface PermissionChecker {

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    boolean hasPermission(Context context, String... permissions);

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    boolean hasPermission(Context context, List<String> permissions);

}
