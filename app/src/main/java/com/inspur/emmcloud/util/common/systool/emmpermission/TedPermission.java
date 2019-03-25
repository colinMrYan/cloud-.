package com.inspur.emmcloud.util.common.systool.emmpermission;

import android.content.Context;

public class TedPermission extends TedPermissionBase{
  public static final String TAG=TedPermission.class.getSimpleName();

  public static Builder with(Context context) {
    return new Builder(context);
  }

    public static class Builder extends PermissionBuilder<Builder> {

    private Builder(Context context) {
      super(context);
    }

    public void check() {
      checkPermissions();
    }

  }
}
