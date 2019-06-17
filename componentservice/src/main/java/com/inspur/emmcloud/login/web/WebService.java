package com.inspur.emmcloud.login.web;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.inspur.emmcloud.login.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface WebService extends CoreService {
    void openCamera(Activity activity, String picPath, int requestCode);

    void openScanCode(Activity activity, int requestCode);

    void openScanCode(Fragment fragment, int requestCode);

    void showScanResult(Context context, String result);

    void openGallery(Activity activity, int limit, int requestCode);
}
