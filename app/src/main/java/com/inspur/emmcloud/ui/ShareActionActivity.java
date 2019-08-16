package com.inspur.emmcloud.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by chenmch on 2019/8/14.
 */

public class ShareActionActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        intent.setClass(this, AppSchemeHandleActivity.class);
        int flag = getIntent().getFlags();
        if ((flag & Intent.FLAG_ACTIVITY_NO_HISTORY) != 0) {
            flag &= ~Intent.FLAG_ACTIVITY_NO_HISTORY;
        }
        intent.setFlags(flag);
        startActivity(intent);
        finish();
    }
}
