package com.inspur.emmcloud.util.privates.systool.emmpermission;

import java.util.List;

public interface EmmPermissionListener {

    void onPermissionGranted(List<String> grantPermissions);

    void onPermissionDenied(List<String> deniedPermissions);

}
