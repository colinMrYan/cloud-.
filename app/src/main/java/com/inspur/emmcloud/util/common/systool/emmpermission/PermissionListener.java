package com.inspur.emmcloud.util.common.systool.emmpermission;

import java.util.List;

public interface PermissionListener {

  void onPermissionGranted(List<String> grantPermissions);

  void onPermissionDenied(List<String> deniedPermissions);

}
