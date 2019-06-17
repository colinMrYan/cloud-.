package com.inspur.emmcloud.basemodule.util.systool.emmpermission;

import android.content.Context;

public class EmmPermission extends EmmPermissionBase {

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder extends EmmPermissionBuilder<Builder> {

        private Builder(Context context) {
            super(context);
        }

        public void check() {
            checkPermissions();
        }

    }
}
