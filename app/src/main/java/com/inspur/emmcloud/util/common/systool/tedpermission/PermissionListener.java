package com.inspur.emmcloud.util.common.systool.tedpermission;

import java.util.List;

public interface PermissionListener {

  void onPermissionGranted();

  void onPermissionDenied(List<String> deniedPermissions);

}
