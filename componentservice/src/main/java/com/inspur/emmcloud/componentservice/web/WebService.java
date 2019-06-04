package com.inspur.emmcloud.componentservice.web;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface WebService extends CoreService {
    void openCamera(Activity activity, String picPath, int requestCode);

    void openScanCode(Activity activity, int requestCode);

    void openScanCode(Fragment fragment, int requestCode);

    void openGallery(Activity activity, int limit, int requestCode);
}
