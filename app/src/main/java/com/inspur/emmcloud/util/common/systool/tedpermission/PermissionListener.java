package com.inspur.emmcloud.util.common.systool.tedpermission;

import java.util.List;

public interface PermissionListener {

  void onPermissionGranted(List<String> grantPermissions);

  void onPermissionDenied(List<String> deniedPermissions);

}
